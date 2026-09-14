package com.rhuloe.cbccryocannon.world;

import com.Harbinger.Spore.core.SConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public final class CryoCleanser {
    private static final Map<String, String> FALLBACKS = Map.ofEntries(
        Map.entry("spore:infested_stone", "minecraft:stone"),
        Map.entry("minecraft:mycelium", "minecraft:dirt"),
        Map.entry("spore:infested_dirt", "minecraft:dirt"),
        Map.entry("spore:infested_deepslate", "minecraft:deepslate"),
        Map.entry("spore:infested_sand", "minecraft:sand"),
        Map.entry("spore:infested_gravel", "minecraft:gravel"),
        Map.entry("spore:infested_netherrack", "minecraft:netherrack"),
        Map.entry("spore:infested_end_stone", "minecraft:end_stone"),
        Map.entry("spore:infested_soul_sand", "minecraft:soul_sand"),
        Map.entry("spore:infested_red_sand", "minecraft:red_sand"),
        Map.entry("spore:infested_clay", "minecraft:clay"),
        Map.entry("spore:infested_cobblestone", "minecraft:cobblestone"),
        Map.entry("spore:infested_cobbled_deepslate", "minecraft:cobbled_deepslate"),
        Map.entry("spore:infested_laboratory_block", "spore:lab_block"),
        Map.entry("spore:infested_laboratory_block1", "spore:lab_block1"),
        Map.entry("spore:infested_laboratory_block2", "spore:lab_block2"),
        Map.entry("spore:infested_laboratory_block3", "spore:lab_block3"),
        Map.entry("spore:infested_stone_bricks", "minecraft:stone_bricks"),
        Map.entry("spore:infested_bricks", "minecraft:bricks"));

    private CryoCleanser() {}

    public static void clean(ServerLevel level, Position center, int range) {
        BlockPos origin = BlockPos.containing(center);
        Map<String, String> conversions = configuredConversions();
        BlockPos.betweenClosedStream(origin.offset(-range, -range, -range), origin.offset(range, range, range))
            .filter(pos -> pos.distSqr(origin) <= range * range)
            .forEach(pos -> convert(level, pos, conversions));
        placeSnow(level, origin, range);
    }

    private static void convert(ServerLevel level, BlockPos pos, Map<String, String> conversions) {
        BlockState state = level.getBlockState(pos);
        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        if ("spore:organite".equals(id)) {
            level.destroyBlock(pos, false);
            return;
        }
        String replacement = conversions.get(id);
        if (replacement == null && (id.equals("spore:biomass_block") || id.equals("spore:calcified_biomass_block")
            || id.equals("spore:gastric_biomass_block") || id.equals("spore:sicken_biomass_block")
            || id.equals("spore:membrane_block") || id.equals("spore:rooted_biomass") || id.equals("spore:rooted_mycelium"))) {
            replacement = "spore:freeze_burned_biomass";
        }
        if (replacement == null && id.equals("spore:remains")) replacement = "spore:frozen_remains";
        if (replacement == null && id.equals("spore:bile")) replacement = "spore:crusted_bile";
        if (replacement == null) return;
        Block target = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(replacement));
        if (target == null || target == Blocks.AIR) return;
        level.setBlock(pos, retainProperties(state, target.defaultBlockState()), Block.UPDATE_ALL);
    }

    private static Map<String, String> configuredConversions() {
        Map<String, String> conversions = new HashMap<>(FALLBACKS);
        List<? extends String> configured = SConfig.DATAGEN.block_cleaning.get();
        for (String entry : configured) {
            String[] pair = entry.split("\\|", 2);
            if (pair.length == 2 && ResourceLocation.tryParse(pair[0]) != null
                && ResourceLocation.tryParse(pair[1]) != null) {
                conversions.put(pair[0], pair[1]);
            }
        }
        return conversions;
    }

    private static void placeSnow(ServerLevel level, BlockPos origin, int range) {
        int attempts = Math.max(24, range * range * 2);
        for (int attempt = 0; attempt < attempts; attempt++) {
            int dx = level.random.nextInt(-range, range + 1);
            int dz = level.random.nextInt(-range, range + 1);
            if (dx * dx + dz * dz > range * range) continue;
            for (int dy = range; dy >= -range; dy--) {
                BlockPos support = origin.offset(dx, dy, dz);
                BlockPos snowPos = support.above();
                if (!level.isLoaded(snowPos) || !level.getBlockState(snowPos).isAir()) continue;
                BlockState snow = Blocks.SNOW.defaultBlockState()
                    .setValue(SnowLayerBlock.LAYERS, level.random.nextInt(1, 4));
                if (!snow.canSurvive(level, snowPos)) continue;
                level.setBlock(snowPos, snow, Block.UPDATE_ALL);
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static BlockState retainProperties(BlockState oldState, BlockState newState) {
        for (Property property : oldState.getProperties()) {
            if (newState.hasProperty(property)) newState = newState.setValue(property, oldState.getValue(property));
        }
        return newState;
    }
}