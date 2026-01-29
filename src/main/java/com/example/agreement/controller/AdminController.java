package com.example.agreement.controller;

import com.example.agreement.service.admin.AdminContractService;
import com.example.agreement.service.admin.AdminUserService;
import com.example.agreement.service.dto.request.SuspendContractRequest;
import com.example.agreement.service.dto.userDto.BlockUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminUserService userService;
    private final AdminContractService contractService;

    @PostMapping("/users/{id}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long id, @RequestBody BlockUserRequest request) {
        userService.blockUser(id, request.getReason());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{id}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable Long id) {
        userService.unblockUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{id}/verify-passport")
    public ResponseEntity<Void> verifyPassport(@PathVariable Long id) {
        userService.verifyPassport(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{id}/promote-admin")
    public ResponseEntity<Void> promoteAdmin(@PathVariable Long id) {
        userService.promoteToAdmin(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/contracts/{id}/suspend")
    public ResponseEntity<Void> suspendContract(@PathVariable Long id, @RequestBody SuspendContractRequest request) {
        contractService.suspendContract(id, request.getReason());
        return ResponseEntity.ok().build();
    }
}

