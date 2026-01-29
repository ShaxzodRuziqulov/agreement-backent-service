package com.example.agreement.repository;

import com.example.agreement.entity.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
    Optional<TelegramUser> findByTelegramId(Long telegramId);
    Optional<TelegramUser> findByPhoneNumber(String phoneNumber);
}
