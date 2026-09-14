package com.rhuloe.cbccryocannon.mixin;

import com.rhuloe.cbccryocannon.content.CryoBreechBlockEntity;
import com.rhuloe.cbccryocannon.content.CryoFeederBlockEntity;
import com.rhuloe.cbccryocannon.content.MountedCryoLinkAccess;
import com.rhuloe.cbccryocannon.content.CryoliteShellProjectile;
import com.rhuloe.cbccryocannon.logic.CannonMath;
import com.rhuloe.cbccryocannon.registry.CryoRegistries;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.cannon_control.contraption.MountedBigCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.IBigCannonBlockEntity;
import rbasamoyai.createbigcannons.multiloader.NetworkPlatform;
import rbasamoyai.createbigcannons.munitions.big_cannon.AbstractBigCannonProjectile;
import rbasamoyai.createbigcannons.network.ClientboundUpdateContraptionPacket;

@Mixin(MountedBigCannonContraption.class)
public abstract class MountedBigCannonContraptionMixin implements MountedCryoLinkAccess {
    @Inject(method = "fireShot", at = @At("HEAD"), cancellable = true)
    private void cbcCryoCannon$requireMinimumStructure(ServerLevel level,
        PitchOrientedContraptionEntity entity, CallbackInfo callback) {
        int[] counts = cbcCryoCannon$countSegments();
        if (counts[0] != 0 && (counts[1] < 2 || counts[2] < 2)) {
            callback.cancel();
            return;
        }
        if (counts[0] != 0 && !cbcCryoCannon$hasLoadedShell()) {
            CryoFeederBlockEntity feeder = cbcCryoCannon$findFeeder(level);
            if (feeder != null && feeder.fuel() <= 0) {
                cbcCryoCannon$nonDestructiveFailureEffect(level, entity);
            }
            callback.cancel();
        }
    }

    @Inject(method = "fail", at = @At("HEAD"), cancellable = true)
    private void cbcCryoCannon$preventDestructiveFailure(BlockPos localPos, Level level,
        PitchOrientedContraptionEntity entity, BlockEntity failingBlock, int charges,
        CallbackInfo callback) {
        if (cbcCryoCannon$countSegments()[0] == 0) return;
        if (level instanceof ServerLevel server) {
            cbcCryoCannon$nonDestructiveFailureEffect(server, entity);
        }
        callback.cancel();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void cbcCryoCannon$feedMountedBreech(Level level, PitchOrientedContraptionEntity entity,
        CallbackInfo callback) {
        if (level.isClientSide) return;
        MountedBigCannonContraption cannon = (MountedBigCannonContraption) (Object) this;
        for (Map.Entry<BlockPos, BlockEntity> entry : cannon.presentBlockEntities.entrySet()) {
            if (!(entry.getValue() instanceof CryoBreechBlockEntity breech)
                || breech.linkedFeeder() == null
                || !(level.getBlockEntity(breech.linkedFeeder()) instanceof CryoFeederBlockEntity feeder)) continue;
            java.util.UUID previousLinkId = breech.linkId();
            if (!com.rhuloe.cbccryocannon.content.CryoLinks.ensureMountedIdentity(feeder, breech)) continue;
            if (!java.util.Objects.equals(previousLinkId, breech.linkId())) {
                breech.setLevel(level);
                cbcCryoCannon$syncCannonBlock(level, entity, cannon, entry.getKey(), breech);
            }
            BlockPos targetPos = entry.getKey().relative(cannon.initialOrientation());
            if (!(cannon.presentBlockEntities.get(targetPos) instanceof IBigCannonBlockEntity target)) continue;
            breech.setLevel(level);
            if (target instanceof BlockEntity targetEntity) targetEntity.setLevel(level);
            if (!feeder.tryFeedMounted(breech, target, cannon.initialOrientation())) continue;
            cbcCryoCannon$syncCannonBlock(level, entity, cannon, entry.getKey(), breech);
            cbcCryoCannon$syncCannonBlock(level, entity, cannon, targetPos, (BlockEntity) target);
            break;
        }
    }

    @Inject(method = "getWeightForStress", at = @At("HEAD"), cancellable = true)
    private void cbcCryoCannon$ignoreCryoWeight(CallbackInfoReturnable<Float> callback) {
        if (cbcCryoCannon$countSegments()[0] != 0) callback.setReturnValue(0.0F);
    }

    @Redirect(
        method = "fireShot",
        at = @At(
            value = "INVOKE",
            target = "Lrbasamoyai/createbigcannons/munitions/big_cannon/AbstractBigCannonProjectile;shoot(DDDFF)V"
        )
    )
    private void cbcCryoCannon$applyStructureBallistics(AbstractBigCannonProjectile projectile,
        double x, double y, double z, float velocity, float spread) {
        if (projectile instanceof CryoliteShellProjectile) {
            int[] counts = cbcCryoCannon$countSegments();
            velocity *= (float) CannonMath.velocityMultiplier(counts[1]);
            spread = (float) CannonMath.spreadDegrees(counts[2]);
        }
        projectile.shoot(x, y, z, velocity, spread);
    }

    private int[] cbcCryoCannon$countSegments() {
        Map<BlockPos, StructureBlockInfo> blocks =
            ((MountedBigCannonContraption) (Object) this).getBlocks();
        int chambers = 0;
        int barrels = 0;
        boolean isCryo = false;
        for (StructureBlockInfo info : blocks.values()) {
            if (info.state().is(CryoRegistries.CRYO_BREECH.get())) isCryo = true;
            if (info.state().is(CryoRegistries.CRYO_CHAMBER.get())) chambers++;
            if (info.state().is(CryoRegistries.CRYO_BARREL.get())) barrels++;
        }
        return new int[] {isCryo ? 1 : 0, chambers, barrels};
    }

    private boolean cbcCryoCannon$hasLoadedShell() {
        MountedBigCannonContraption cannon = (MountedBigCannonContraption) (Object) this;
        for (BlockEntity blockEntity : cannon.presentBlockEntities.values()) {
            if (blockEntity instanceof IBigCannonBlockEntity cannonBlock
                && cannonBlock.cannonBehavior().block().state().is(CryoRegistries.CRYOLITE_SHELL.get())) {
                return true;
            }
        }
        return false;
    }

    private CryoFeederBlockEntity cbcCryoCannon$findFeeder(Level level) {
        MountedBigCannonContraption cannon = (MountedBigCannonContraption) (Object) this;
        for (BlockEntity blockEntity : cannon.presentBlockEntities.values()) {
            if (blockEntity instanceof CryoBreechBlockEntity breech && breech.linkedFeeder() != null
                && level.getBlockEntity(breech.linkedFeeder()) instanceof CryoFeederBlockEntity feeder) {
                return feeder;
            }
        }
        return null;
    }

    @Override
    public boolean cbcCryoCannon$detachFeeder(Level level, PitchOrientedContraptionEntity entity,
        BlockPos feederPos) {
        MountedBigCannonContraption cannon = (MountedBigCannonContraption) (Object) this;
        for (Map.Entry<BlockPos, BlockEntity> entry : cannon.presentBlockEntities.entrySet()) {
            if (!(entry.getValue() instanceof CryoBreechBlockEntity breech)
                || !feederPos.equals(breech.linkedFeeder())) continue;
            breech.setLevel(level);
            breech.setLinkedFeeder(null);
            cbcCryoCannon$syncCannonBlock(level, entity, cannon, entry.getKey(), breech);
            return true;
        }
        return false;
    }

    private void cbcCryoCannon$nonDestructiveFailureEffect(ServerLevel level,
        PitchOrientedContraptionEntity entity) {
        MountedBigCannonContraption cannon = (MountedBigCannonContraption) (Object) this;
        Vec3 effectPosition = entity.position();
        for (Map.Entry<BlockPos, BlockEntity> entry : cannon.presentBlockEntities.entrySet()) {
            if (entry.getValue() instanceof CryoBreechBlockEntity) {
                effectPosition = entity.toGlobalVector(Vec3.atCenterOf(entry.getKey()).add(0, 0.5D, 0), 0);
                break;
            }
        }
        level.playSound(null, effectPosition.x, effectPosition.y, effectPosition.z,
            SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0F, 0.9F);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, effectPosition.x, effectPosition.y, effectPosition.z,
            1, 0, 0, 0, 0);

        BlockPos mount = entity.getController() instanceof BlockEntity controller
            ? controller.getBlockPos() : entity.blockPosition();
        for (int attempt = 0; attempt < 40; attempt++) {
            int dx = level.random.nextInt(9) - 4;
            int dz = level.random.nextInt(9) - 4;
            if (dx * dx + dz * dz > 16) continue;
            for (int dy = 3; dy >= -4; dy--) {
                BlockPos target = mount.offset(dx, dy, dz);
                if (!level.isLoaded(target)) continue;
                BlockState existing = level.getBlockState(target);
                if (!existing.isAir() && !existing.is(Blocks.SNOW)) continue;
                int layers = existing.is(Blocks.SNOW)
                    ? Math.min(8, existing.getValue(SnowLayerBlock.LAYERS) + level.random.nextInt(1, 4))
                    : level.random.nextInt(1, 4);
                BlockState snow = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers);
                if (!snow.canSurvive(level, target)) continue;
                level.setBlock(target, snow, Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
                break;
            }
        }
    }

    private void cbcCryoCannon$syncCannonBlock(Level level, PitchOrientedContraptionEntity entity,
        MountedBigCannonContraption cannon, BlockPos localPos, BlockEntity blockEntity) {
        StructureBlockInfo oldInfo = cannon.getBlocks().get(localPos);
        if (oldInfo == null) return;
        CompoundTag tag = blockEntity.saveWithFullMetadata(level.registryAccess());
        tag.remove("x");
        tag.remove("y");
        tag.remove("z");
        StructureBlockInfo newInfo = new StructureBlockInfo(oldInfo.pos(), oldInfo.state(), tag);
        cannon.getBlocks().put(localPos, newInfo);
        NetworkPlatform.sendToClientTracking(ClientboundUpdateContraptionPacket.entity(entity, localPos, newInfo),
            entity);
    }
}