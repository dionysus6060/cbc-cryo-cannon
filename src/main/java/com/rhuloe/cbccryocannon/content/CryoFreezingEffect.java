package com.rhuloe.cbccryocannon.content;

import com.rhuloe.cbccryocannon.config.CryoConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public final class CryoFreezingEffect extends MobEffect {
    public CryoFreezingEffect() {
        super(MobEffectCategory.HARMFUL, 0x79D8FF);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.setTicksFrozen(Math.max(entity.getTicksFrozen(), CryoConfig.VALUES.freezeDuration.get()));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false, true));
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}