package com.rhuloe.cbccryocannon.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

public interface MountedCryoLinkAccess {
    boolean cbcCryoCannon$detachFeeder(Level level, PitchOrientedContraptionEntity entity, BlockPos feederPos);
}