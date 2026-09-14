package com.rhuloe.cbccryocannon.mixin;

import com.rhuloe.cbccryocannon.content.CryoliteShellProjectile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.effects.particles.plumes.BigCannonPlumeParticle;

@Mixin(BigCannonPlumeParticle.class)
public abstract class BigCannonPlumeParticleMixin {
    @Unique private ClientLevel cbcCryoCannon$level;
    @Unique private Vec3 cbcCryoCannon$origin;
    @Unique private Vec3 cbcCryoCannon$direction;
    @Unique private boolean cbcCryoCannon$cryoPlume;
    @Unique private int cbcCryoCannon$checks;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void cbcCryoCannon$capturePlume(ClientLevel level, double x, double y, double z,
        double dx, double dy, double dz, float size, float power, CallbackInfo callback) {
        cbcCryoCannon$level = level;
        cbcCryoCannon$origin = new Vec3(x, y, z);
        cbcCryoCannon$direction = new Vec3(dx, dy, dz).normalize();
        cbcCryoCannon$detectCryoProjectile();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void cbcCryoCannon$refreshProjectileMatch(CallbackInfo callback) {
        if (!cbcCryoCannon$cryoPlume && cbcCryoCannon$checks++ < 5) {
            cbcCryoCannon$detectCryoProjectile();
        }
    }

    @ModifyArg(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
        ),
        index = 0
    )
    private ParticleOptions cbcCryoCannon$replacePonderFlame(ParticleOptions particle) {
        return cbcCryoCannon$blueFlame(particle);
    }

    @ModifyArg(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;ZDDDDDD)V"
        ),
        index = 0
    )
    private ParticleOptions cbcCryoCannon$replaceWorldFlame(ParticleOptions particle) {
        return cbcCryoCannon$blueFlame(particle);
    }

    @Unique
    private ParticleOptions cbcCryoCannon$blueFlame(ParticleOptions particle) {
        return cbcCryoCannon$cryoPlume && particle == ParticleTypes.FLAME
            ? ParticleTypes.SOUL_FIRE_FLAME : particle;
    }

    @Unique
    private void cbcCryoCannon$detectCryoProjectile() {
        if (cbcCryoCannon$level == null || cbcCryoCannon$origin == null) return;
        AABB search = new AABB(cbcCryoCannon$origin, cbcCryoCannon$origin).inflate(48.0D);
        for (CryoliteShellProjectile projectile :
            cbcCryoCannon$level.getEntitiesOfClass(CryoliteShellProjectile.class, search)) {
            Vec3 offset = projectile.position().subtract(cbcCryoCannon$origin);
            double forward = offset.dot(cbcCryoCannon$direction);
            double perpendicularSqr = offset.lengthSqr() - forward * forward;
            if (forward >= -2.0D && forward <= 48.0D && perpendicularSqr <= 16.0D) {
                cbcCryoCannon$cryoPlume = true;
                return;
            }
        }
    }
}