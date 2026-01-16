package com.example.agreement.service;

import com.example.agreement.entity.User;
import com.example.agreement.entity.enumerated.VerificationStatus;
import com.example.agreement.exeption.UnauthorizedException;
import com.example.agreement.exeption.UserNotFoundException;
import com.example.agreement.repository.UserRepository;

import com.example.agreement.service.dto.auth.UpdateProfileDto;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private static final String UPLOAD_DIR = "uploads/passports/";

    public User getCurrentUser() {
        String phoneNumber = SecurityContextHolder.getContext()
                .getAuthentication().getName();

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
    public User uploadPassportFront(MultipartFile file) throws IOException {
        User user = getCurrentUser();
        String filePath = saveFile(file, "front");
        user.setPassportFrontPath(filePath);
        user.setPassportStatus(VerificationStatus.PENDING);
        return userRepository.save(user);
    }

    @Transactional
    public User uploadPassportBack(MultipartFile file) throws IOException {
        User user = getCurrentUser();
        String filePath = saveFile(file, "back");
        user.setPassportBackPath(filePath);
        user.setPassportStatus(VerificationStatus.PENDING);
        return userRepository.save(user);
    }

    private String saveFile(MultipartFile file, String side) throws IOException {
        // Create directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = UUID.randomUUID().toString() + "_" + side + extension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return UPLOAD_DIR + filename;
    }

    @Transactional
    public void deleteAccount() {
        User user = getCurrentUser();
        user.setStatus(User.UserStatus.DELETED);
        userRepository.save(user);
        log.info("User account deleted: {}", user.getPhoneNumber());
    }
}