package com.rhuloe.cbccryocannon.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlockEntity;

public final class CryoCannonBlockEntity extends BigCannonBlockEntity {
    private int topologyRefreshCooldown;

    public CryoCannonBlockEntity(BlockEntityType<? extends BigCannonBlockEntity> type, BlockPos pos,
        BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide || level.getBlockEntity(worldPosition) != this) return;
        if (topologyRefreshCooldown-- <= 0) {
            topologyRefreshCooldown = 20;
            CryoCannonTubeBlock.refreshBarrel(level, worldPosition);
        }
    }
}