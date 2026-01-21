package com.example.agreement.service;

import com.example.agreement.entity.Asset;
import com.example.agreement.entity.User;
import com.example.agreement.entity.enumerated.AssetStatus;
import com.example.agreement.repository.AssetRepository;
import com.example.agreement.repository.UserRepository;
import com.example.agreement.service.dto.assetDto.AssetCreateDto;
import com.example.agreement.service.dto.assetDto.AssetResponseDto;
import com.example.agreement.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository repository;
    private final UserRepository userRepository;

    @Transactional
    public AssetResponseDto create(AssetCreateDto dto) {
        User owner = getCurrentUser();

        Asset asset = new Asset();
        asset.setOwner(owner);
        asset.setType(dto.getType());
        asset.setDescription(dto.getDescription());
        asset.setUsageRules(dto.getUsageRules());
        asset.setDamageLiability(dto.getDamageLiability());
        asset.setAssetStatus(AssetStatus.AVAILABLE);

        Asset saved = repository.save(asset);
        return toDto(saved);
    }

    @Transactional
    public AssetResponseDto update(Long assetId, AssetCreateDto dto) {
        User owner = getCurrentUser();

        Asset asset = repository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found: " + assetId));

        if (!asset.getOwner().getId().equals(owner.getId())) {
            throw new IllegalStateException("Access denied");
        }

        if (asset.getAssetStatus() != AssetStatus.AVAILABLE) {
            throw new IllegalStateException("Only AVAILABLE asset can be updated");
        }

        asset.setType(dto.getType());
        asset.setDescription(dto.getDescription());
        asset.setUsageRules(dto.getUsageRules());
        asset.setDamageLiability(dto.getDamageLiability());

        return toDto(asset); // ✅ dirty-checking ishlaydi
    }

    public AssetResponseDto findById(Long id) {
        return toDto(getAsset(id));
    }

    public List<AssetResponseDto> findMyAssets() {
        Long ownerId = SecurityUtils.currentUserId();

        return repository.findByOwnerId(ownerId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public Asset getAsset(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found: " + id));
    }

    public Asset getOwnedAsset(Long id) {
        User owner = getCurrentUser();

        Asset asset = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found: " + id));

        if (!asset.getOwner().getId().equals(owner.getId())) {
            throw new IllegalStateException("Access denied");
        }

        return asset;
    }

    /* ================= helpers ================= */

    private User getCurrentUser() {
        return userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private AssetResponseDto toDto(Asset asset) {
        AssetResponseDto dto = new AssetResponseDto();
        dto.setId(asset.getId());
        dto.setType(asset.getType());
        dto.setDescription(asset.getDescription());
        dto.setAssetStatus(asset.getAssetStatus());
        dto.setUsageRules(asset.getUsageRules());
        dto.setDamageLiability(asset.getDamageLiability());
        return dto;
    }
}
