package com.rhuloe.cbccryocannon.client;

import com.rhuloe.cbccryocannon.content.CryoCannonTubeBlock;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour.CTContext;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour.ContextRequirement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.cannons.big_cannons.BuiltUpCannonCTBehavior;

final class CryoBarrelCTBehavior extends BuiltUpCannonCTBehavior {
    CryoBarrelCTBehavior(CTSpriteShiftEntry shift) {
        super(shift);
    }

    @Override
    public CTContext buildContext(BlockAndTintGetter level, BlockPos pos, BlockState state,
        Direction face, ContextRequirement requirement) {
        CTContext context = super.buildContext(level, pos, state, face, requirement);
        if (state.hasProperty(CryoCannonTubeBlock.SEGMENT)
            && state.getValue(CryoCannonTubeBlock.SEGMENT) == CryoCannonTubeBlock.Segment.END_OPPOSITE) {
            boolean up = context.up;
            context.up = context.down;
            context.down = up;
        }
        return context;
    }
}