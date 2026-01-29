package com.example.agreement.service.admin;

import com.example.agreement.entity.User;
import com.example.agreement.repository.UserRepository;
import com.example.agreement.entity.enumerated.UserBlockType;
import com.example.agreement.entity.enumerated.VerificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional
    public void blockUser(Long userId, String reason) {
        User user = getUser(userId);

        user.setBlockType(UserBlockType.ACTION_BLOCK);
        user.setBlockReason(reason);
        user.setBlockedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Transactional
    public void unblockUser(Long userId) {
        User user = getUser(userId);

        user.setBlockType(UserBlockType.NONE);
        user.setBlockReason(null);
        user.setBlockedAt(null);

        userRepository.save(user);
    }

    @Transactional
    public void verifyPassport(Long userId) {
        User user = getUser(userId);

        if (user.getPassportFrontPath() == null) {
            throw new IllegalStateException("Passport not submitted");
        }

        user.setPassportStatus(VerificationStatus.VERIFIED);
        userRepository.save(user);
    }

    @Transactional
    public void promoteToAdmin(Long userId) {
        User user = getUser(userId);
        user.setRole(User.UserRole.ADMIN);
        userRepository.save(user);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

