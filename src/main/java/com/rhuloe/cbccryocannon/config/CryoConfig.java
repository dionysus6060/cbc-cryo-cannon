package com.rhuloe.cbccryocannon.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class CryoConfig {
    public static final ModConfigSpec SPEC;
    public static final Values VALUES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        VALUES = new Values(builder);
        SPEC = builder.build();
    }

    private CryoConfig() {}

    public static void register(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, SPEC);
    }

    public static final class Values {
        public final ModConfigSpec.IntValue impactDamage;
        public final ModConfigSpec.DoubleValue explosionRadius;
        public final ModConfigSpec.IntValue cleanseRadius;
        public final ModConfigSpec.IntValue freezeDuration;
        public final ModConfigSpec.IntValue canisterFuel;
        public final ModConfigSpec.IntValue shotFuel;
        public final ModConfigSpec.IntValue reloadTicks;
        public final ModConfigSpec.IntValue minimumRpm;
        public final ModConfigSpec.IntValue stressImpact;
        public final ModConfigSpec.BooleanValue rpmAffectsReloadRate;
        public final ModConfigSpec.IntValue maximumLinkLength;

        Values(ModConfigSpec.Builder b) {
            b.push("projectile");
            impactDamage = b.defineInRange("impact_damage", 12, 0, 1024);
            explosionRadius = b.defineInRange("explosion_radius", 4.0D, 0D, 64D);
            cleanseRadius = b.defineInRange("cleanse_radius", 4, 0, 64);
            freezeDuration = b.defineInRange("freeze_duration_ticks", 140, 1, 12000);
            b.pop();
            b.push("feeder");
            canisterFuel = b.defineInRange("canister_fuel", 24000, 1, 1000000);
            shotFuel = b.defineInRange("shot_fuel", 1000, 1, 1000000);
            reloadTicks = b.defineInRange("reload_ticks", 120, 1, 12000);
            minimumRpm = b.defineInRange("minimum_rpm", 32, 1, 256);
            stressImpact = b.defineInRange("stress_impact", 16, 0, 16384);
            rpmAffectsReloadRate = b.define("rpm_affects_reload_rate", false);
            maximumLinkLength = b.defineInRange("maximum_link_length", 16, 1, 128);
            b.pop();
        }
    }
}