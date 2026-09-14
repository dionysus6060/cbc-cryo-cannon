package com.rhuloe.cbccryocannon.content;

import com.rhuloe.cbccryocannon.CbcCryoCannon;
import net.minecraft.resources.ResourceLocation;
import rbasamoyai.createbigcannons.cannons.big_cannons.material.BigCannonMaterial;
import rbasamoyai.createbigcannons.cannons.big_cannons.material.BigCannonMaterialProperties;
import rbasamoyai.createbigcannons.cannons.big_cannons.material.BigCannonMaterialProperties.FailureMode;

public final class CryoCannonMaterials {
    public static final BigCannonMaterial CRYO = BigCannonMaterial.register(
        ResourceLocation.fromNamespaceAndPath(CbcCryoCannon.MOD_ID, "cryo"),
        BigCannonMaterialProperties.builder()
            .minimumVelocityPerBarrel(1.0D)
            .weight(0.0F)
            .maxSafePropellantStress(1_000_000)
            .failureMode(FailureMode.RUPTURE)
            .connectsInSurvival(true)
            .isWeldable(true)
            .weldDamage(1)
            .weldStressPenalty(1)
            .minimumSpread(0.5F)
            .spreadReductionPerBarrel(0.8333333F)
            .build()
    );

    private CryoCannonMaterials() {}
}