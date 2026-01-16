package com.example.agreement.service.dto.assetDto;

import com.example.agreement.entity.enumerated.AssetStatus;
import com.example.agreement.entity.enumerated.AssetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetDto {
    private Long id;
    private Long ownerId;
    private AssetType type;
    private String description;
    private AssetStatus assetStatus;
    private String usageRules;
    private String damageLiability;
}
