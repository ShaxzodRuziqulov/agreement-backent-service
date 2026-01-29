package com.example.agreement.service.telegram;

import com.example.agreement.entity.TelegramUser;
import com.example.agreement.entity.User;
import com.example.agreement.entity.enumerated.OtpType;
import com.example.agreement.repository.UserRepository;
import com.example.agreement.service.AuthService;
import com.example.agreement.service.OtpService;
import com.example.agreement.service.dto.auth.AuthResponseDto;
import com.example.agreement.service.dto.auth.VerifyOtpDto;
import com.example.agreement.service.dto.auth.VerifyRegistrationDto;
import com.example.agreement.service.dto.assetDto.AssetResponseDto;
import com.example.agreement.service.dto.contractDto.ContractResponseDto;
import com.example.agreement.service.dto.paymentDto.PaymentResponseDto;
import com.example.agreement.service.telegram.dto.TelegramContact;
import com.example.agreement.service.telegram.dto.TelegramMessage;
import com.example.agreement.service.telegram.dto.TelegramUpdate;
import com.example.agreement.util.PhoneUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramBotHandler {

    private final TelegramApiClient apiClient;
    private final TelegramUserService telegramUserService;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final AuthService authService;
    private final BotBackendClient backendClient;

    private final Map<Long, BotSession> sessions = new ConcurrentHashMap<>();

    public void handleUpdate(TelegramUpdate update) {
        TelegramMessage message = update.getMessage();
        if (message == null || message.getChat() == null || message.getFrom() == null) {
            return;
        }

        Long chatId = message.getChat().getId();
        Long telegramId = message.getFrom().getId();
        BotSession session = sessions.computeIfAbsent(telegramId, id -> new BotSession());
        if (session.getPhoneNumber() == null) {
            telegramUserService.findByTelegramId(telegramId)
                    .map(TelegramUser::getPhoneNumber)
                    .ifPresent(session::setPhoneNumber);
        }

        TelegramContact contact = message.getContact();
        if (contact != null) {
            handleContact(chatId, message, contact, session);
            return;
        }

        String text = message.getText();
        if (text == null || text.isBlank()) {
            return;
        }

        if (isBackCommand(text)) {
            goBack(chatId, session);
            return;
        }

        if ("/start".equalsIgnoreCase(text.trim())) {
            sendWelcome(chatId, session);
            return;
        }

        if (handleState(chatId, text, session)) {
            return;
        }

        handleCommand(chatId, text, session);
    }

    private void handleContact(Long chatId, TelegramMessage message, TelegramContact contact, BotSession session) {
        if (contact.getUserId() != null && !contact.getUserId().equals(message.getFrom().getId())) {
            apiClient.sendMessage(chatId, "Iltimos, faqat o'zingizning kontaktni yuboring.");
            return;
        }

        String phone = PhoneUtils.normalize(contact.getPhoneNumber());
        String username = message.getFrom().getUsername();
        TelegramUser linked = telegramUserService.linkTelegramToPhone(message.getFrom().getId(), username, phone);

        session.setPhoneNumber(linked.getPhoneNumber());
        setState(session, BotSession.State.NONE);

        apiClient.sendMessage(chatId, "Telefon raqami bog'landi: " + linked.getPhoneNumber(), mainMenuKeyboard(session));
    }

    private boolean handleState(Long chatId, String text, BotSession session) {
        switch (session.getState()) {
            case WAIT_FIRST_NAME -> {
                session.setFirstName(text.trim());
                setState(session, BotSession.State.WAIT_LAST_NAME);
                apiClient.sendMessage(chatId, "Familiyangizni yuboring:");
                return true;
            }
            case WAIT_LAST_NAME -> {
                session.setLastName(text.trim());
                if (session.getPhoneNumber() == null) {
                    apiClient.sendMessage(chatId, "Telefon raqam topilmadi. Iltimos, kontakt yuboring.", contactKeyboard());
                    setState(session, BotSession.State.WAIT_CONTACT);
                    return true;
                }
                if (userRepository.existsByPhoneNumber(session.getPhoneNumber())) {
                    apiClient.sendMessage(chatId, "Bu raqam allaqachon ro'yxatdan o'tgan. Login qiling.");
                    setState(session, BotSession.State.NONE);
                    return true;
                }
                String code = otpService.generateOtp(session.getPhoneNumber(), OtpType.REGISTRATION);
                apiClient.sendMessage(chatId, "Tasdiqlash kodi (Telegram): " + code);
                apiClient.sendMessage(chatId, "Kodini yuboring (6 ta raqam):");
                setState(session, BotSession.State.WAIT_OTP_REGISTER);
                return true;
            }
            case WAIT_OTP_LOGIN -> {
                if (!text.matches("\\d{6}")) {
                    apiClient.sendMessage(chatId, "OTP 6 ta raqam bo'lishi kerak. Qayta yuboring.");
                    return true;
                }
                try {
                    VerifyOtpDto dto = new VerifyOtpDto();
                    dto.setPhoneNumber(session.getPhoneNumber());
                    dto.setCode(text);
                    AuthResponseDto response = authService.verifyLoginOtp(dto);
                    session.setToken(response.getToken());
                    session.setUserId(response.getUserId());
                    setState(session, BotSession.State.NONE);
                    apiClient.sendMessage(chatId, "Kirish muvaffaqiyatli.", mainMenuKeyboard(session));
                } catch (Exception e) {
                    apiClient.sendMessage(chatId, "OTP xato yoki eskirgan. Qayta urinib ko'ring.");
                }
                return true;
            }
            case WAIT_OTP_REGISTER -> {
                if (!text.matches("\\d{6}")) {
                    apiClient.sendMessage(chatId, "OTP 6 ta raqam bo'lishi kerak. Qayta yuboring.");
                    return true;
                }
                try {
                    VerifyRegistrationDto dto = new VerifyRegistrationDto();
                    dto.setPhoneNumber(session.getPhoneNumber());
                    dto.setCode(text);
                    dto.setFirstName(session.getFirstName());
                    dto.setLastName(session.getLastName());
                    AuthResponseDto response = authService.verifyRegistrationOtp(dto);
                    session.setToken(response.getToken());
                    session.setUserId(response.getUserId());
                    setState(session, BotSession.State.NONE);
                    apiClient.sendMessage(chatId, "Ro'yxatdan o'tish muvaffaqiyatli.", mainMenuKeyboard(session));
                } catch (Exception e) {
                    apiClient.sendMessage(chatId, "OTP xato yoki eskirgan. Qayta urinib ko'ring.");
                }
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void handleCommand(Long chatId, String text, BotSession session) {
        String normalized = text.trim().toLowerCase(Locale.ROOT);

        if (normalized.equals("login") || normalized.equals("kirish")) {
            if (session.getToken() != null) {
                apiClient.sendMessage(chatId, "Siz allaqachon login qilgansiz.", mainMenuKeyboard(session));
                return;
            }
            handleLogin(chatId, session);
            return;
        }

        if (normalized.equals("register") || normalized.equals("ro'yxatdan o'tish") || normalized.equals("registratsiya")) {
            if (session.getToken() != null) {
                apiClient.sendMessage(chatId, "Siz allaqachon login qilgansiz.", mainMenuKeyboard(session));
                return;
            }
            handleRegister(chatId, session);
            return;
        }

        if (normalized.equals("profil")) {
            handleProfile(chatId, session);
            return;
        }

        if (normalized.equals("shartnomalar")) {
            handleContracts(chatId, session);
            return;
        }

        if (normalized.equals("assetlar") || normalized.equals("asset") || normalized.equals("assets")) {
            if (session.getToken() == null) {
                apiClient.sendMessage(chatId, "Avval login qiling.");
                return;
            }
            handleAssets(chatId, session);
            return;
        }

        if (normalized.equals("to'lovlar") || normalized.equals("tolovlar")) {
            if (session.getToken() == null) {
                apiClient.sendMessage(chatId, "Avval login qiling.");
                return;
            }
            handlePayments(chatId, session);
            return;
        }

        if (normalized.equals("logout") || normalized.equals("chiqish")) {
            session.setToken(null);
            session.setUserId(null);
            apiClient.sendMessage(chatId, "Chiqildi.", mainMenuKeyboard(session));
            return;
        }

        apiClient.sendMessage(chatId, "Buyruqni tanlang yoki /start yuboring.", mainMenuKeyboard(session));
    }

    private void handleLogin(Long chatId, BotSession session) {
        if (session.getPhoneNumber() == null) {
            apiClient.sendMessage(chatId, "Avval telefon raqamni yuboring.", contactKeyboard());
            setState(session, BotSession.State.WAIT_CONTACT);
            return;
        }

        if (!userRepository.existsByPhoneNumber(session.getPhoneNumber())) {
            apiClient.sendMessage(chatId, "Bu raqam ro'yxatdan o'tmagan. Avval registratsiya qiling.");
            return;
        }

        String code = otpService.generateOtp(session.getPhoneNumber(), OtpType.LOGIN);
        apiClient.sendMessage(chatId, "Tasdiqlash kodi (Telegram): " + code);
        apiClient.sendMessage(chatId, "Kodini yuboring (6 ta raqam):");
        setState(session, BotSession.State.WAIT_OTP_LOGIN);
    }

    private void handleRegister(Long chatId, BotSession session) {
        if (session.getPhoneNumber() == null) {
            apiClient.sendMessage(chatId, "Avval telefon raqamni yuboring.", contactKeyboard());
            setState(session, BotSession.State.WAIT_CONTACT);
            return;
        }

        if (userRepository.existsByPhoneNumber(session.getPhoneNumber())) {
            apiClient.sendMessage(chatId, "Bu raqam allaqachon ro'yxatdan o'tgan. Login qiling.");
            return;
        }

        setState(session, BotSession.State.WAIT_FIRST_NAME);
        apiClient.sendMessage(chatId, "Ismingizni yuboring:");
    }

    private void handleProfile(Long chatId, BotSession session) {
        if (session.getToken() == null) {
            apiClient.sendMessage(chatId, "Avval login qiling.");
            return;
        }

        try {
            User user = backendClient.getCurrentUser(session.getToken());
            apiClient.sendMessage(chatId, formatUser(user));
        } catch (Exception e) {
            apiClient.sendMessage(chatId, "Profilni olishda xatolik. Qayta login qiling.");
        }
    }

    private void handleContracts(Long chatId, BotSession session) {
        if (session.getToken() == null) {
            apiClient.sendMessage(chatId, "Avval login qiling.");
            return;
        }

        try {
            List<ContractResponseDto> contracts = backendClient.getMyContracts(session.getToken());
            if (contracts == null || contracts.isEmpty()) {
                apiClient.sendMessage(chatId, "Shartnomalar topilmadi.");
                return;
            }
            apiClient.sendMessage(chatId, formatContracts(contracts, session.getUserId()));
        } catch (Exception e) {
            apiClient.sendMessage(chatId, "Shartnomalarni olishda xatolik.");
        }
    }

    private void handleAssets(Long chatId, BotSession session) {
        try {
            List<AssetResponseDto> assets = backendClient.getMyAssets(session.getToken());
            apiClient.sendMessage(chatId, formatAssets(assets));
        } catch (Exception e) {
            apiClient.sendMessage(chatId, "Assetlarni olishda xatolik.");
        }
    }

    private void handlePayments(Long chatId, BotSession session) {
        try {
            List<PaymentResponseDto> payments = backendClient.getMyPayments(session.getToken());
            if (payments == null || payments.isEmpty()) {
                apiClient.sendMessage(chatId, "To'lovlar topilmadi.");
                return;
            }
            apiClient.sendMessage(chatId, formatPayments(payments));
        } catch (Exception e) {
            apiClient.sendMessage(chatId, "To'lovlarni olishda xatolik.");
        }
    }

    private void sendWelcome(Long chatId, BotSession session) {
        apiClient.sendMessage(chatId, "Assalomu alaykum. Bitimchi botga xush kelibsiz.");
        if (session.getToken() != null) {
            apiClient.sendMessage(chatId, "Siz allaqachon tizimdasiz.", mainMenuKeyboard(session));
            return;
        }
        if (session.getPhoneNumber() == null) {
            apiClient.sendMessage(chatId, "Telefon raqamingizni yuboring.", contactKeyboard());
            return;
        }
        apiClient.sendMessage(chatId, "Telefon raqamingiz bog'langan. Login yoki Register tanlang.",
                mainMenuKeyboard(session));
    }

    private Map<String, Object> contactKeyboard() {
        Map<String, Object> button = new HashMap<>();
        button.put("text", "Telefon raqamni yuborish");
        button.put("request_contact", true);

        Map<String, Object> markup = new HashMap<>();
        markup.put("keyboard", List.of(List.of(button)));
        markup.put("resize_keyboard", true);
        markup.put("one_time_keyboard", true);
        return markup;
    }

    private Map<String, Object> mainMenuKeyboard(BotSession session) {
        Map<String, Object> b1 = button("Login");
        Map<String, Object> b2 = button("Register");
        Map<String, Object> b3 = button("Profil");
        Map<String, Object> b4 = button("Shartnomalar");
        Map<String, Object> b5 = button("Assetlar");
        Map<String, Object> b6 = button("To'lovlar");
        Map<String, Object> b7 = button("Logout");

        Map<String, Object> markup = new HashMap<>();
        if (session.getToken() == null) {
            markup.put("keyboard", List.of(
                    List.of(b1, b2)
            ));
        } else {
            markup.put("keyboard", List.of(
                    List.of(b3, b4),
                    List.of(b5, b6),
                    List.of(b7)
            ));
        }
        markup.put("resize_keyboard", true);
        return markup;
    }

    private Map<String, Object> button(String text) {
        Map<String, Object> button = new HashMap<>();
        button.put("text", text);
        return button;
    }

    private String formatUser(User user) {
        if (user == null) {
            return "Profil topilmadi.";
        }
        return "User: " + user.getId()
                + "\nTelefon: " + user.getPhoneNumber()
                + "\nIsm: " + safe(user.getFirstName())
                + "\nFamiliya: " + safe(user.getLastName())
                + "\nRole: " + user.getRole().name()
                + "\nStatus: " + user.getStatus().name();
    }

    private String formatContracts(List<ContractResponseDto> contracts, Long currentUserId) {
        StringBuilder sb = new StringBuilder();
        sb.append("Shartnomalar:\n");
        for (ContractResponseDto c : contracts) {
            sb.append("- Status: ").append(c.getStatus())
                    .append(" | Asset: ").append(c.getAssetId())
                    .append(" | Amount: ").append(c.getBillingAmount())
                    .append(" ").append(c.getBillingUnit())
                    .append(" | Prepaid: ").append(c.getPrepaidPeriods())
                    .append(" | Lang: ").append(c.getLanguage())
                    .append(" | Start: ").append(c.getStartAt())
                    .append(" | Role: ").append(resolveRole(c, currentUserId))
                    .append("\n");
        }
        return sb.toString();
    }

    private String formatPayments(List<PaymentResponseDto> payments) {
        StringBuilder sb = new StringBuilder();
        sb.append("To'lovlar:\n");
        for (PaymentResponseDto p : payments) {
            sb.append("- Amount: ").append(p.getAmount())
                    .append(" | Status: ").append(p.getStatus())
                    .append(" | Method: ").append(p.getMethod())
                    .append(" | Provider: ").append(p.getProvider())
                    .append(" | Contract: ").append(p.getContractId())
                    .append("\n");
        }
        return sb.toString();
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }

    private String resolveRole(ContractResponseDto c, Long currentUserId) {
        if (currentUserId == null) {
            return "-";
        }
        if (currentUserId.equals(c.getOwnerId())) {
            return "OWNER";
        }
        if (currentUserId.equals(c.getRenterId())) {
            return "RENTER";
        }
        return "-";
    }

    private String formatAssets(List<AssetResponseDto> assets) {
        if (assets == null || assets.isEmpty()) {
            return "Assetlar topilmadi.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Assetlar:\n");
        for (AssetResponseDto a : assets) {
            sb.append("- Type: ").append(a.getType())
                    .append(" | Status: ").append(a.getAssetStatus())
                    .append(" | Desc: ").append(safe(a.getDescription()))
                    .append("\n");
        }
        return sb.toString();
    }

    private void setState(BotSession session, BotSession.State newState) {
        if (session.getState() != newState) {
            if (newState == BotSession.State.NONE) {
                session.getHistory().clear();
            } else {
                session.getHistory().push(session.getState());
            }
            session.setState(newState);
        }
    }

    private void goBack(Long chatId, BotSession session) {
        if (session.getHistory().isEmpty()) {
            session.setState(BotSession.State.NONE);
            apiClient.sendMessage(chatId, "Bosh menyu.", mainMenuKeyboard(session));
            return;
        }
        BotSession.State previous = session.getHistory().pop();
        session.setState(previous);
        promptForState(chatId, session);
    }

    private void promptForState(Long chatId, BotSession session) {
        if (Objects.requireNonNull(session.getState()) == BotSession.State.WAIT_CONTACT) {
            apiClient.sendMessage(chatId, "Telefon raqamingizni yuboring.", contactKeyboard());
        } else {
            apiClient.sendMessage(chatId, "Bosh menyu.", mainMenuKeyboard(session));
        }
    }

    private boolean isBackCommand(String text) {
        String t = text.trim().toLowerCase(Locale.ROOT);
        return t.equals("orqaga") || t.equals("back");
    }
}
