package com.example.agreement.controller;

import com.example.agreement.service.AssetService;
import com.example.agreement.service.dto.assetDto.AssetCreateDto;
import com.example.agreement.service.dto.assetDto.AssetResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService service;

    @PostMapping
    public ResponseEntity<AssetResponseDto> create(@Valid @RequestBody AssetCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetResponseDto> update(@PathVariable Long id,
                                                   @Valid @RequestBody AssetCreateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/my")
    public ResponseEntity<List<AssetResponseDto>> myAssets() {
        return ResponseEntity.ok(service.findMyAssets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
}