package com.example.agreement.service.dto.assetDto;

import com.example.agreement.entity.enumerated.AssetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetCreateDto {

    private AssetType type;
    private String description;
    private String usageRules;
    private String damageLiability;
}

