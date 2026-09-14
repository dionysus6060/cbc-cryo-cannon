package com.rhuloe.cbccryocannon.client;

import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.rhuloe.cbccryocannon.CbcCryoCannon;
import com.rhuloe.cbccryocannon.registry.CryoRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import rbasamoyai.createbigcannons.cannons.big_cannons.BuiltUpCannonCTBehavior;
import rbasamoyai.createbigcannons.connected_textures.CBCCTSpriteShifter;
import rbasamoyai.createbigcannons.index.CBCCTTypes;
import rbasamoyai.createbigcannons.munitions.big_cannon.BigCannonProjectileRenderer;

@Mod(value = CbcCryoCannon.MOD_ID, dist = Dist.CLIENT)
public final class CbcCryoCannonClient {
    private static final CTSpriteShiftEntry CRYO_BARREL_TEXTURES = CBCCTSpriteShifter.getCT(
        CBCCTTypes.CANNON,
        1,
        texture("block/cannon_barrel/cryo_cannon_barrel_side"),
        texture("block/cannon_barrel/cryo_cannon_barrel_side_connected")
    );
    private static final CTSpriteShiftEntry CRYO_CHAMBER_TEXTURES = CBCCTSpriteShifter.getCT(
        CBCCTTypes.CANNON,
        1,
        texture("block/cannon_chamber/cryo_cannon_chamber_side"),
        texture("block/cannon_chamber/cryo_cannon_chamber_side_connected")
    );

    public CbcCryoCannonClient(IEventBus modBus) {
        modBus.addListener(CbcCryoCannonClient::clientSetup);
        modBus.addListener(CbcCryoCannonClient::registerRenderers);
        modBus.addListener(CbcCryoCannonClient::registerMenuScreens);
        NeoForge.EVENT_BUS.addListener(CryoHoseWorldRenderer::render);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CreateRegistrate.connectedTextures(
                () -> new CryoBarrelCTBehavior(CRYO_BARREL_TEXTURES)
            ).accept(CryoRegistries.CRYO_BARREL.get());
            CreateRegistrate.connectedTextures(
                () -> new BuiltUpCannonCTBehavior(CRYO_CHAMBER_TEXTURES)
            ).accept(CryoRegistries.CRYO_CHAMBER.get());
        });
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(CbcCryoCannon.MOD_ID, path);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CryoRegistries.CRYOLITE_PROJECTILE.get(), BigCannonProjectileRenderer::new);
        event.registerBlockEntityRenderer(CryoRegistries.CRYO_FEEDER_ENTITY.get(), CryoHoseRenderer::new);
    }

    private static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(CryoRegistries.CRYO_FEEDER_MENU.get(), CryoFeederScreen::new);
    }
}