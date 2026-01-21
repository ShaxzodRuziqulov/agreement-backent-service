package com.example.agreement.controller;

import com.example.agreement.service.ContractService;
import com.example.agreement.service.dto.contractDto.ContractCreateDto;
import com.example.agreement.service.dto.contractDto.ContractResponseDto;
import com.example.agreement.service.dto.userDto.PinflSubmitDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService service;

    @PostMapping
    public ResponseEntity<ContractResponseDto> create(@RequestBody ContractCreateDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<ContractResponseDto>> getMyContracts() {
        return ResponseEntity.ok(service.findMyContracts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping("/{contractId}/renter-submit")
    public ResponseEntity<Void> renterSubmit(@PathVariable Long contractId, @RequestBody PinflSubmitDto request) {
        service.renterSubmit(contractId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{contractId}/owner-approve")
    public ResponseEntity<Void> ownerApprove(@PathVariable Long contractId) {
        service.ownerApprove(contractId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<Void> close(@PathVariable Long id) {
        service.closeContract(id);
        return ResponseEntity.ok().build();
    }
}
