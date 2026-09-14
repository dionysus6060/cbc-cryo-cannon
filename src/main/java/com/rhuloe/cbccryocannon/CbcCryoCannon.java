package com.rhuloe.cbccryocannon;

import com.rhuloe.cbccryocannon.config.CryoConfig;
import com.rhuloe.cbccryocannon.registry.CryoRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(CbcCryoCannon.MOD_ID)
public final class CbcCryoCannon {
    public static final String MOD_ID = "cbc_cryo_cannon";

    public CbcCryoCannon(IEventBus bus, ModContainer modContainer) {
        CryoRegistries.register(bus);
        bus.addListener(this::commonSetup);
        CryoConfig.register(modContainer);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(CryoRegistries::registerMunitionProperties);
    }
}