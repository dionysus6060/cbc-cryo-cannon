package com.rhuloe.cbccryocannon.content;

import com.rhuloe.cbccryocannon.registry.CryoRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.munitions.big_cannon.he_shell.HEShellBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.he_shell.HEShellProjectile;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;

/** A CBC shell block using CBC's existing fuze block entity contract. */
public final class CryoliteShellBlock extends HEShellBlock {
    public CryoliteShellBlock(Properties properties) {
        super(properties);
    }

    @Override
    public EntityType<? extends HEShellProjectile> getAssociatedEntityType() {
        return CryoRegistries.CRYOLITE_PROJECTILE.get();
    }

    @Override
    public BlockEntityType<? extends FuzedBlockEntity> getBlockEntityType() {
        return CryoRegistries.CRYOLITE_SHELL_ENTITY.get();
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
        BlockEntityType<T> type) {
        return type == CryoRegistries.CRYOLITE_SHELL_ENTITY.get()
            ? (world, pos, blockState, entity) -> ((FuzedBlockEntity) entity).tick()
            : null;
    }
}