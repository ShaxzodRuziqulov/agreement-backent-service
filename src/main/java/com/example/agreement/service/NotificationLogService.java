package com.example.agreement.service;

import com.example.agreement.entity.NotificationLog;
import com.example.agreement.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationLogService {

    private final NotificationLogRepository repository;
    private final UserService userService;
    private final ContractService contractService;



    public List<NotificationLog> findByUser(Long userId) {
        return repository.findByUserIdOrderBySentAtDesc(userId);
    }
}