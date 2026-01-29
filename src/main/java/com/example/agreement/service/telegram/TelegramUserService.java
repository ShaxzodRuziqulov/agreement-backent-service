package com.example.agreement.service.telegram;

import com.example.agreement.entity.TelegramUser;
import com.example.agreement.repository.TelegramUserRepository;
import com.example.agreement.util.PhoneUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TelegramUserService {

    private final TelegramUserRepository repository;

    public Optional<TelegramUser> findByTelegramId(Long telegramId) {
        return repository.findByTelegramId(telegramId);
    }

    @Transactional
    public TelegramUser linkTelegramToPhone(Long telegramId, String username, String phoneNumber) {
        String normalized = PhoneUtils.normalize(phoneNumber);

        TelegramUser user = repository.findByTelegramId(telegramId)
                .orElseGet(TelegramUser::new);

        user.setTelegramId(telegramId);
        user.setUsername(username);
        user.setPhoneNumber(normalized);

        return repository.save(user);
    }
}
