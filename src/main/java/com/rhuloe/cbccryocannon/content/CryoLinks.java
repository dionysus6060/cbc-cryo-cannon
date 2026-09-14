package com.rhuloe.cbccryocannon.content;

import net.minecraft.core.BlockPos;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.MountedBigCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

public final class CryoLinks {
    private CryoLinks() {}

    public static boolean link(Level level, BlockPos first, BlockPos second) {
        BlockEntity firstEntity = level.getBlockEntity(first);
        BlockEntity secondEntity = level.getBlockEntity(second);
        CryoFeederBlockEntity feeder = firstEntity instanceof CryoFeederBlockEntity f ? f
            : secondEntity instanceof CryoFeederBlockEntity f ? f : null;
        CryoBreechBlockEntity breech = firstEntity instanceof CryoBreechBlockEntity b ? b
            : secondEntity instanceof CryoBreechBlockEntity b ? b : null;
        clearStaleLink(level, feeder);
        clearStaleLink(level, breech);
        if (feeder == null || breech == null || feeder.linkedBreech() != null || breech.linkedFeeder() != null) return false;
        UUID linkId = UUID.randomUUID();
        feeder.setLink(breech.getBlockPos(), linkId);
        breech.setLink(feeder.getBlockPos(), linkId, breech.getBlockPos());
        return true;
    }

    public static boolean ensureStationaryIdentity(CryoFeederBlockEntity feeder, CryoBreechBlockEntity breech) {
        if (!breech.getBlockPos().equals(feeder.linkedBreech())
            || !feeder.getBlockPos().equals(breech.linkedFeeder())) return false;
        if (breech.linkAnchor() == null) {
            breech.setLink(breech.linkedFeeder(), breech.linkId(), breech.getBlockPos());
        } else if (!breech.getBlockPos().equals(breech.linkAnchor())) {
            return false;
        }
        return migrateIdentity(feeder, breech);
    }

    public static boolean ensureMountedIdentity(CryoFeederBlockEntity feeder, CryoBreechBlockEntity breech) {
        if (breech.linkAnchor() == null || !breech.linkAnchor().equals(feeder.linkedBreech())
            || !feeder.getBlockPos().equals(breech.linkedFeeder())) return false;
        return migrateIdentity(feeder, breech);
    }

    private static boolean migrateIdentity(CryoFeederBlockEntity feeder, CryoBreechBlockEntity breech) {
        if (feeder.linkId() == null && breech.linkId() == null) {
            UUID linkId = UUID.randomUUID();
            feeder.setLink(feeder.linkedBreech(), linkId);
            breech.setLink(breech.linkedFeeder(), linkId);
            return true;
        }
        if (feeder.linkId() == null) {
            feeder.setLink(feeder.linkedBreech(), breech.linkId());
        } else if (breech.linkId() == null) {
            breech.setLink(breech.linkedFeeder(), feeder.linkId(), breech.linkAnchor());
        }
        return Objects.equals(feeder.linkId(), breech.linkId());
    }

    private static void clearStaleLink(Level level, CryoFeederBlockEntity feeder) {
        if (feeder == null || feeder.linkedBreech() == null) return;
        BlockEntity partner = level.getBlockEntity(feeder.linkedBreech());
        if (!(partner instanceof CryoBreechBlockEntity breech)
            || !feeder.getBlockPos().equals(breech.linkedFeeder())) {
            feeder.setLinkedBreech(null);
        }
    }

    private static void clearStaleLink(Level level, CryoBreechBlockEntity breech) {
        if (breech == null || breech.linkedFeeder() == null) return;
        BlockEntity partner = level.getBlockEntity(breech.linkedFeeder());
        if (!(partner instanceof CryoFeederBlockEntity feeder)
            || !breech.getBlockPos().equals(feeder.linkedBreech())) {
            breech.setLinkedFeeder(null);
        }
    }

    public static boolean detach(Level level, BlockPos endpoint) {
        BlockEntity entity = level.getBlockEntity(endpoint);
        BlockPos partner = null;
        if (entity instanceof CryoFeederBlockEntity feeder) {
            partner = feeder.linkedBreech();
            if (partner == null) return false;
            feeder.setLinkedBreech(null);
        } else if (entity instanceof CryoBreechBlockEntity breech) {
            partner = breech.linkedFeeder();
            if (partner == null) return false;
            breech.setLinkedFeeder(null);
        } else {
            return false;
        }
        BlockEntity other = level.getBlockEntity(partner);
        if (other instanceof CryoFeederBlockEntity feeder) feeder.setLinkedBreech(null);
        if (other instanceof CryoBreechBlockEntity breech) breech.setLinkedFeeder(null);
        if (entity instanceof CryoFeederBlockEntity && level instanceof ServerLevel serverLevel) {
            detachMountedBreech(serverLevel, endpoint);
        }
        return true;
    }

    private static void detachMountedBreech(ServerLevel level, BlockPos feederPos) {
        for (Entity rawEntity : level.getAllEntities()) {
            if (rawEntity instanceof PitchOrientedContraptionEntity entity
                && entity.getContraption() instanceof MountedBigCannonContraption cannon
                && cannon instanceof MountedCryoLinkAccess access
                && access.cbcCryoCannon$detachFeeder(level, entity, feederPos)) {
                return;
            }
        }
    }
}