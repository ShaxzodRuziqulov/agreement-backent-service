package com.example.agreement.service;

import com.example.agreement.entity.User;
import com.example.agreement.entity.enumerated.VerificationStatus;
import com.example.agreement.exeption.UnauthorizedException;
import com.example.agreement.exeption.UserNotFoundException;
import com.example.agreement.repository.UserRepository;
import com.example.agreement.service.dto.auth.UpdateProfileDto;
import com.example.agreement.service.dto.userDto.PassportUploadResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private static final String UPLOAD_DIR = "uploads/passports/";

    public User getCurrentUser() {
        String phoneNumber = Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getName();

        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Transactional
    public User updateProfile(UpdateProfileDto dto) {
        User user = getCurrentUser();

        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }

        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }

        if (dto.getPinfl() != null) {
            // Check if PINFL already exists
            userRepository.findByPinfl(dto.getPinfl())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(user.getId())) {
                            throw new IllegalArgumentException("PINFL already registered");
                        }
                    });
            user.setPinfl(dto.getPinfl());
        }

        return userRepository.save(user);
    }

    @Transactional
    public PassportUploadResponseDto uploadPassport(MultipartFile file, String side) throws IOException {

        User user = getCurrentUser();

        if (!"front".equalsIgnoreCase(side) && !"back".equalsIgnoreCase(side)) {
            throw new IllegalStateException("side must be front or back");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalStateException("File is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalStateException("Only JPG/PNG images are allowed");
        }

        long maxSize = 5L * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalStateException("File size must be <= 5MB");
        }

        String filePath = saveFile(file, side.toLowerCase());

        if ("front".equalsIgnoreCase(side)) {
            user.setPassportFrontPath(filePath);
        } else {
            user.setPassportBackPath(filePath);
        }

        user.setPassportStatus(VerificationStatus.PENDING);

        User saved = userRepository.save(user);

        return new PassportUploadResponseDto(
                saved.getId(),
                saved.getPassportFrontPath(),
                saved.getPassportBackPath(),
                saved.getPassportStatus()
        );
    }

    private String saveFile(MultipartFile file, String side) throws IOException {

        Path uploadPath = Paths.get(UPLOAD_DIR).normalize().toAbsolutePath();
        Files.createDirectories(uploadPath);

        String originalFilename = file.getOriginalFilename();
        String extension = ".jpg";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        if (!extension.equals(".jpg") && !extension.equals(".jpeg") && !extension.equals(".png")) {
            throw new IllegalStateException("Only .jpg, .jpeg, .png allowed");
        }

        String filename = UUID.randomUUID() + "_" + side + extension;

        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "uploads/" + filename;
    }


    @Transactional
    public void deleteAccount() {
        User user = getCurrentUser();
        user.setStatus(User.UserStatus.DELETED);
        userRepository.save(user);
        log.info("User account deleted: {}", user.getPhoneNumber());
    }
}