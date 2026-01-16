package com.example.agreement.service;

import com.example.agreement.entity.Asset;
import com.example.agreement.entity.User;
import com.example.agreement.repository.AssetRepository;
import com.example.agreement.repository.UserRepository;
import com.example.agreement.service.dto.assetDto.AssetCreateDto;
import com.example.agreement.service.dto.assetDto.AssetResponseDto;
import com.example.agreement.entity.enumerated.AssetStatus;
import com.example.agreement.util.SecurityUtils;
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

        return toDto(repository.save(asset));
    }

    @Transactional
    public AssetResponseDto update(Long id, AssetCreateDto dto) {
        Asset asset = getOwnedAsset(id);

        if (asset.getAssetStatus() != AssetStatus.AVAILABLE) {
            throw new IllegalStateException("Only AVAILABLE asset can be updated");
        }

        asset.setDescription(dto.getDescription());
        asset.setUsageRules(dto.getUsageRules());
        asset.setDamageLiability(dto.getDamageLiability());

        return toDto(asset); // dirty-checking yetarli
    }


    public AssetResponseDto findById(Long id) {
        return toDto(getAsset(id));
    }

    public List<AssetResponseDto> findMyAssets() {
        Long userId = getCurrentUser().getId();
        return repository.findByOwnerId(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /* ===== helpers ===== */

    public Asset getAsset(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));
    }

    private Asset getOwnedAsset(Long id) {
        Asset asset = getAsset(id);
        if (!asset.getOwner().getId().equals(getCurrentUser().getId())) {
            throw new IllegalStateException("Access denied");
        }
        return asset;
    }

    private User getCurrentUser() {
        return userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
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
