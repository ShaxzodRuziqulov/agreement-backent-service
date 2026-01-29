package com.example.agreement.controller;

import com.example.agreement.service.ContractService;
import com.example.agreement.service.dto.contractDto.ContractActivateRequest;
import com.example.agreement.service.dto.contractDto.ContractCreateDto;
import com.example.agreement.service.dto.contractDto.ContractResponseDto;
import com.example.agreement.service.dto.paymentDto.ClaimResponseDto;
import com.example.agreement.service.dto.userDto.PinflSubmitDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contracts")
public class ContractController {

    private final ContractService contractService;

    // =========================
    // 1) CREATE CONTRACT (DRAFT)
    // =========================
    @PostMapping
    public ResponseEntity<ContractResponseDto> create(@RequestBody ContractCreateDto dto) {
        return ResponseEntity.ok(contractService.create(dto));
    }

    // =========================
    // 2) SEND TO RENTER
    // DRAFT -> WAITING_RENTER_INFO
    // =========================
    @PostMapping("/{id}/send-to-renter")
    public ResponseEntity<Void> sendToRenter(@PathVariable Long id) {
        contractService.sendToRenter(id);
        return ResponseEntity.ok().build();
    }

    // =========================
    // 3) RENTER SUBMIT INFO
    // WAITING_RENTER_INFO -> WAITING_OWNER_APPROVAL
    // =========================
    @PostMapping("/{id}/renter-submit")
    public ResponseEntity<Void> renterSubmit(@PathVariable Long id, @RequestBody PinflSubmitDto request) {
        contractService.renterSubmit(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/verify-renter")
    public ResponseEntity<Void> verifyRenterIdentity(@PathVariable Long id) {
        contractService.verifyRenterIdentity(id);
        return ResponseEntity.ok().build();
    }

    // =========================
    // 4) OWNER APPROVE
    // WAITING_OWNER_APPROVAL -> APPROVED
    // =========================
    @PostMapping("/{id}/owner-approve")
    public ResponseEntity<Void> ownerApprove(@PathVariable Long id) {
        contractService.ownerApprove(id);
        return ResponseEntity.ok().build();
    }

    // =========================
    // 5) ACTIVATE CONTRACT
    // APPROVED -> ACTIVE
    // + initial claim + prepaid payment
    // =========================
    @PostMapping("/{id}/activate")
    public ResponseEntity<ClaimResponseDto> activate(@PathVariable Long id, @RequestBody ContractActivateRequest request) {
        return ResponseEntity.ok(contractService.activate(id, request));
    }

    // =========================
    // 6) CLOSE CONTRACT
    // ACTIVE/LATE/SUSPENDED -> CLOSED
    // =========================
    @PostMapping("/{id}/close")
    public ResponseEntity<Void> close(
            @PathVariable Long id
    ) {
        contractService.closeContract(id);
        return ResponseEntity.ok().build();
    }

    // =========================
    // 7) GET BY ID
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<ContractResponseDto> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(contractService.findById(id));
    }

    // =========================
    // 8) GET MY CONTRACTS
    // =========================
    @GetMapping("/my")
    public ResponseEntity<List<ContractResponseDto>> getMyContracts() {
        return ResponseEntity.ok(contractService.findMyContracts());
    }
}
