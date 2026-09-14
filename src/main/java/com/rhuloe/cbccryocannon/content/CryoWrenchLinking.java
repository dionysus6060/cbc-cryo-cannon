package com.rhuloe.cbccryocannon.content;

import com.rhuloe.cbccryocannon.config.CryoConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Stores the pending cannon endpoint on Create's wrench. The hose itself is
 * built into the feeder and breech, so linking never consumes an item.
 */
public final class CryoWrenchLinking {
    private static final String FIRST_POS = "CbcCryoCannonLinkPos";
    private static final String FIRST_DIMENSION = "CbcCryoCannonLinkDimension";

    private CryoWrenchLinking() {}

    public static InteractionResult useOnEndpoint(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockPos clicked = context.getClickedPos();
        BlockEntity clickedEntity = level.getBlockEntity(clicked);
        if (!isEndpoint(clickedEntity)) return InteractionResult.PASS;

        ItemStack wrench = context.getItemInHand();
        CompoundTag data = wrench.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (isLinked(clickedEntity)) {
            CryoLinks.detach(level, clicked);
            clearSelection(wrench, data);
            player.displayClientMessage(Component.translatable("message.cbc_cryo_cannon.hose_unlinked"), true);
            return InteractionResult.SUCCESS;
        }

        if (!data.contains(FIRST_POS)) {
            saveSelection(wrench, data, level, clicked);
            player.displayClientMessage(Component.translatable("message.cbc_cryo_cannon.hose_first"), true);
            return InteractionResult.SUCCESS;
        }

        BlockPos first = BlockPos.of(data.getLong(FIRST_POS));
        if (first.equals(clicked)) {
            player.displayClientMessage(Component.translatable("message.cbc_cryo_cannon.hose_self"), true);
            return InteractionResult.SUCCESS;
        }

        String dimension = level.dimension().location().toString();
        if (!dimension.equals(data.getString(FIRST_DIMENSION)) || !level.isLoaded(first)) {
            saveSelection(wrench, data, level, clicked);
            player.displayClientMessage(Component.translatable("message.cbc_cryo_cannon.hose_first_reset"), true);
            return InteractionResult.SUCCESS;
        }

        BlockEntity firstEntity = level.getBlockEntity(first);
        if (!isEndpoint(firstEntity)) {
            saveSelection(wrench, data, level, clicked);
            player.displayClientMessage(Component.translatable("message.cbc_cryo_cannon.hose_first_reset"), true);
            return InteractionResult.SUCCESS;
        }

        double maximumLength = CryoConfig.VALUES.maximumLinkLength.get();
        if (first.distSqr(clicked) > maximumLength * maximumLength) {
            clearSelection(wrench, data);
            player.displayClientMessage(Component.translatable("message.cbc_cryo_cannon.hose_too_far"), true);
            return InteractionResult.SUCCESS;
        }

        if (!oppositeEndpointTypes(firstEntity, clickedEntity) || !CryoLinks.link(level, first, clicked)) {
            clearSelection(wrench, data);
            player.displayClientMessage(Component.translatable("message.cbc_cryo_cannon.hose_failed"), true);
            return InteractionResult.SUCCESS;
        }

        clearSelection(wrench, data);
        player.displayClientMessage(Component.translatable("message.cbc_cryo_cannon.hose_linked"), true);
        return InteractionResult.SUCCESS;
    }

    private static boolean isEndpoint(BlockEntity entity) {
        return entity instanceof CryoFeederBlockEntity || entity instanceof CryoBreechBlockEntity;
    }

    private static boolean isLinked(BlockEntity entity) {
        return entity instanceof CryoFeederBlockEntity feeder && feeder.linkedBreech() != null
            || entity instanceof CryoBreechBlockEntity breech && breech.linkedFeeder() != null;
    }

    private static boolean oppositeEndpointTypes(BlockEntity first, BlockEntity second) {
        return first instanceof CryoFeederBlockEntity && second instanceof CryoBreechBlockEntity
            || first instanceof CryoBreechBlockEntity && second instanceof CryoFeederBlockEntity;
    }

    private static void saveSelection(ItemStack wrench, CompoundTag data, Level level, BlockPos pos) {
        data.putLong(FIRST_POS, pos.asLong());
        data.putString(FIRST_DIMENSION, level.dimension().location().toString());
        wrench.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    private static void clearSelection(ItemStack wrench, CompoundTag data) {
        data.remove(FIRST_POS);
        data.remove(FIRST_DIMENSION);
        if (data.isEmpty()) {
            wrench.remove(DataComponents.CUSTOM_DATA);
        } else {
            wrench.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        }
    }
}