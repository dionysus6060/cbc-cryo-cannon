package com.rhuloe.cbccryocannon.registry;

import com.rhuloe.cbccryocannon.CbcCryoCannon;
import com.rhuloe.cbccryocannon.content.CryoFreezingEffect;
import com.rhuloe.cbccryocannon.content.CryoBreechBlock;
import com.rhuloe.cbccryocannon.content.CryoBreechBlockEntity;
import com.rhuloe.cbccryocannon.content.CryoCannonBlockEntity;
import com.rhuloe.cbccryocannon.content.CryoCannonBlockItem;
import com.rhuloe.cbccryocannon.content.CryoCannonTubeBlock;
import com.rhuloe.cbccryocannon.content.CryoFeederBlock;
import com.rhuloe.cbccryocannon.content.CryoFeederBlockEntity;
import com.rhuloe.cbccryocannon.content.CryoFeederMenu;
import com.rhuloe.cbccryocannon.content.CryoliteShellBlock;
import com.rhuloe.cbccryocannon.content.CryoliteShellProjectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlockEntity;
import rbasamoyai.createbigcannons.crafting.casting.CannonCastShape;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;
import rbasamoyai.createbigcannons.munitions.FuzedProjectileBlockItem;
import rbasamoyai.createbigcannons.munitions.config.MunitionPropertiesHandler;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;

public final class CryoRegistries {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, CbcCryoCannon.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, CbcCryoCannon.MOD_ID);
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, CbcCryoCannon.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, CbcCryoCannon.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CbcCryoCannon.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CbcCryoCannon.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, CbcCryoCannon.MOD_ID);

    public static final DeferredHolder<Block, CryoBreechBlock> CRYO_BREECH = BLOCKS.register("cryo_breech",
        () -> new CryoBreechBlock(BlockBehaviour.Properties.of().strength(4.5F)
            .requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredHolder<Block, CryoCannonTubeBlock> CRYO_CHAMBER = BLOCKS.register("cryo_chamber",
        () -> new CryoCannonTubeBlock(BlockBehaviour.Properties.of().strength(4.5F)
            .requiresCorrectToolForDrops().noOcclusion(),
            () -> CannonCastShape.MEDIUM, Shapes.block(), false));
    public static final DeferredHolder<Block, CryoCannonTubeBlock> CRYO_BARREL = BLOCKS.register("cryo_barrel",
        () -> new CryoCannonTubeBlock(BlockBehaviour.Properties.of().strength(4.5F).requiresCorrectToolForDrops(),
            () -> CannonCastShape.VERY_SMALL, Block.box(2, 0, 2, 14, 16, 14), true));
    public static final DeferredHolder<Block, CryoFeederBlock> CRYO_FEEDER = BLOCKS.register("cryo_feeder",
        () -> new CryoFeederBlock(BlockBehaviour.Properties.of().strength(4.0F)
            .requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredHolder<Block, Block> CRYO_HOSE_DECORATION = BLOCKS.register("cryo_hose_decoration",
        () -> new Block(BlockBehaviour.Properties.of().noOcclusion().air()));
    public static final DeferredHolder<Block, CryoliteShellBlock> CRYOLITE_SHELL = BLOCKS.register("cryolite_shell",
        () -> new CryoliteShellBlock(BlockBehaviour.Properties.of().strength(1.5F).requiresCorrectToolForDrops()));

    public static final DeferredHolder<Item, Item> CRYO_BREECH_ITEM = ITEMS.register("cryo_breech",
        () -> new CryoCannonBlockItem<>(
            CRYO_BREECH.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CRYO_CHAMBER_ITEM = ITEMS.register("cryo_chamber",
        () -> new CryoCannonBlockItem<>(CRYO_CHAMBER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CRYO_BARREL_ITEM = ITEMS.register("cryo_barrel",
        () -> new CryoCannonBlockItem<>(CRYO_BARREL.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CRYO_FEEDER_ITEM = ITEMS.register("cryo_feeder",
        () -> new BlockItem(CRYO_FEEDER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CRYOLITE_SHELL_ITEM = ITEMS.register("cryolite_shell",
        () -> new FuzedProjectileBlockItem(CRYOLITE_SHELL.get(), new Item.Properties()));
    public static final DeferredHolder<MobEffect, CryoFreezingEffect> CRYO_FREEZING = EFFECTS.register("cryo_freezing",
        CryoFreezingEffect::new);
    public static final DeferredHolder<EntityType<?>, EntityType<CryoliteShellProjectile>> CRYOLITE_PROJECTILE =
        ENTITIES.register("cryolite_shell", () -> EntityType.Builder
            .of(CryoliteShellProjectile::new, MobCategory.MISC).sized(0.8F, 0.8F).clientTrackingRange(4)
            .updateInterval(1).build("cbc_cryo_cannon:cryolite_shell"));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CryoFeederBlockEntity>> CRYO_FEEDER_ENTITY =
        BLOCK_ENTITIES.register("cryo_feeder", () -> BlockEntityType.Builder
            .of(CryoFeederBlockEntity::new, CRYO_FEEDER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BigCannonBlockEntity>> CRYO_CANNON_ENTITY =
        BLOCK_ENTITIES.register("cryo_cannon", () -> BlockEntityType.Builder
            .of(CryoRegistries::makeCannonEntity, CRYO_CHAMBER.get(), CRYO_BARREL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CryoBreechBlockEntity>> CRYO_BREECH_ENTITY =
        BLOCK_ENTITIES.register("cryo_breech", () -> BlockEntityType.Builder
            .of(CryoRegistries::makeBreechEntity, CRYO_BREECH.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FuzedBlockEntity>> CRYOLITE_SHELL_ENTITY =
        BLOCK_ENTITIES.register("cryolite_shell", () -> BlockEntityType.Builder
            .of(CryoRegistries::makeShellEntity, CRYOLITE_SHELL.get()).build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<CryoFeederMenu>> CRYO_FEEDER_MENU =
        MENUS.register("cryo_feeder", () -> IMenuTypeExtension.create(CryoFeederMenu::new));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.cbc_cryo_cannon"))
            .icon(() -> new ItemStack(CRYOLITE_SHELL_ITEM.get()))
            .displayItems((parameters, output) -> {
                output.accept(CRYO_BREECH_ITEM.get());
                output.accept(CRYO_CHAMBER_ITEM.get());
                output.accept(CRYO_BARREL_ITEM.get());
                output.accept(CRYO_FEEDER_ITEM.get());
                output.accept(CRYOLITE_SHELL_ITEM.get());
            }).build());

    private CryoRegistries() {}

    public static void registerMunitionProperties() {
        MunitionPropertiesHandler.registerProjectileHandler(CRYOLITE_PROJECTILE.get(),
            CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE);
    }

    private static BigCannonBlockEntity makeCannonEntity(net.minecraft.core.BlockPos pos,
        net.minecraft.world.level.block.state.BlockState state) {
        return new CryoCannonBlockEntity(CRYO_CANNON_ENTITY.get(), pos, state);
    }

    private static CryoBreechBlockEntity makeBreechEntity(net.minecraft.core.BlockPos pos,
        net.minecraft.world.level.block.state.BlockState state) {
        return new CryoBreechBlockEntity(CRYO_BREECH_ENTITY.get(), pos, state);
    }

    private static FuzedBlockEntity makeShellEntity(net.minecraft.core.BlockPos pos,
        net.minecraft.world.level.block.state.BlockState state) {
        return new FuzedBlockEntity(CRYOLITE_SHELL_ENTITY.get(), pos, state);
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        EFFECTS.register(bus);
        ENTITIES.register(bus);
        BLOCK_ENTITIES.register(bus);
        TABS.register(bus);
        MENUS.register(bus);
        bus.addListener(CryoRegistries::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CRYO_FEEDER_ENTITY.get(),
            (feeder, side) -> feeder.inventory());
    }
}