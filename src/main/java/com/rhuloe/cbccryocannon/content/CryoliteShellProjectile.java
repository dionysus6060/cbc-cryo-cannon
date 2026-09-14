package com.rhuloe.cbccryocannon.content;

import com.rhuloe.cbccryocannon.config.CryoConfig;
import com.rhuloe.cbccryocannon.registry.CryoRegistries;
import com.rhuloe.cbccryocannon.world.CryoCleanser;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile.ImpactResult;
import rbasamoyai.createbigcannons.munitions.ProjectileContext;
import rbasamoyai.createbigcannons.munitions.big_cannon.he_shell.HEShellProjectile;

public final class CryoliteShellProjectile extends HEShellProjectile {
    private boolean detonated;

    public CryoliteShellProjectile(EntityType<? extends HEShellProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void detonate(Position position) {
        if (detonated) return;
        detonated = true;
        if (!(level() instanceof ServerLevel server)) return;
        float radius = Math.max(4.0F, CryoConfig.VALUES.explosionRadius.get().floatValue());
        server.explode(this, position.x(), position.y(), position.z(), radius, Level.ExplosionInteraction.TNT);
        int range = CryoConfig.VALUES.cleanseRadius.get();
        AABB area = new AABB(position.x(), position.y(), position.z(), position.x(), position.y(), position.z()).inflate(range);
        for (LivingEntity entity : server.getEntitiesOfClass(LivingEntity.class, area)) {
            if (entity.distanceToSqr(position.x(), position.y(), position.z()) <= 4.0D) {
                entity.hurt(server.damageSources().explosion(this, this),
                    CryoConfig.VALUES.impactDamage.get().floatValue());
            }
            entity.addEffect(new MobEffectInstance(CryoRegistries.CRYO_FREEZING,
                CryoConfig.VALUES.freezeDuration.get(), 0));
            entity.setTicksFrozen(Math.max(entity.getTicksFrozen(), CryoConfig.VALUES.freezeDuration.get()));
        }
        CryoCleanser.clean(server, position, range);
        server.sendParticles(ParticleTypes.SNOWFLAKE, position.x(), position.y(), position.z(), 48,
            0.65D, 0.65D, 0.65D, 0.04D);
        server.sendParticles(ParticleTypes.END_ROD, position.x(), position.y(), position.z(), 24,
            0.45D, 0.45D, 0.45D, 0.03D);
    }

    @Override
    protected boolean onImpact(HitResult hitResult, ImpactResult impactResult, ProjectileContext context) {
        super.onImpact(hitResult, impactResult, context);
        detonate(hitResult.getLocation());
        return true;
    }

    @Override
    public BlockState getRenderedBlockState() {
        return CryoRegistries.CRYOLITE_SHELL.get().defaultBlockState()
            .setValue(BlockStateProperties.FACING, Direction.NORTH);
    }
}