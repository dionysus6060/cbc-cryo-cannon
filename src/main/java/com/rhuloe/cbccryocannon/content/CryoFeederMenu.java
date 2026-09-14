package com.rhuloe.cbccryocannon.content;

import com.rhuloe.cbccryocannon.config.CryoConfig;
import com.rhuloe.cbccryocannon.registry.CryoRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class CryoFeederMenu extends AbstractContainerMenu {
    public static final int FEEDER_SLOTS = 10;
    private static final int PLAYER_INVENTORY_START = FEEDER_SLOTS;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_SLOTS_END = PLAYER_HOTBAR_START + 9;

    private final CryoFeederBlockEntity feeder;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public CryoFeederMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, findFeeder(playerInventory, buffer.readBlockPos()),
            new SimpleContainerData(5));
    }

    public CryoFeederMenu(int containerId, Inventory playerInventory, CryoFeederBlockEntity feeder) {
        this(containerId, playerInventory, feeder, feederData(feeder));
    }

    private CryoFeederMenu(int containerId, Inventory playerInventory, CryoFeederBlockEntity feeder,
        ContainerData data) {
        super(CryoRegistries.CRYO_FEEDER_MENU.get(), containerId);
        this.feeder = feeder;
        this.access = ContainerLevelAccess.create(feeder.getLevel(), feeder.getBlockPos());
        this.data = data;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new SlotItemHandler(feeder.inventory(), column + row * 3,
                    44 + column * 18, 20 + row * 18));
            }
        }
        addSlot(new SlotItemHandler(feeder.inventory(), CryoFeederBlockEntity.FUEL_SLOT, 116, 38));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9,
                    8 + column * 18, 102 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 160));
        }
        addDataSlots(data);
    }

    public int fuel() {
        return combineWords(data.get(0), data.get(1));
    }

    public int fuelCapacity() {
        return Math.max(1, combineWords(data.get(3), data.get(4)));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, CryoRegistries.CRYO_FEEDER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < FEEDER_SLOTS) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_SLOTS_END, true)) return ItemStack.EMPTY;
        } else if (feeder.inventory().isItemValid(CryoFeederBlockEntity.FUEL_SLOT, stack)) {
            if (!moveItemStackTo(stack, CryoFeederBlockEntity.FUEL_SLOT,
                CryoFeederBlockEntity.FUEL_SLOT + 1, false)) return ItemStack.EMPTY;
        } else if (feeder.inventory().isItemValid(0, stack)) {
            if (!moveItemStackTo(stack, 0, CryoFeederBlockEntity.SHELL_SLOTS, false)) return ItemStack.EMPTY;
        } else if (index < PLAYER_HOTBAR_START) {
            if (!moveItemStackTo(stack, PLAYER_HOTBAR_START, PLAYER_SLOTS_END, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, stack);
        return copy;
    }

    private static CryoFeederBlockEntity findFeeder(Inventory playerInventory, BlockPos pos) {
        BlockEntity entity = playerInventory.player.level().getBlockEntity(pos);
        if (entity instanceof CryoFeederBlockEntity feeder) return feeder;
        throw new IllegalStateException("Cryo-Feeder menu opened without a Cryo-Feeder at " + pos);
    }

    private static ContainerData feederData(CryoFeederBlockEntity feeder) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> lowWord(feeder.fuel());
                    case 1 -> highWord(feeder.fuel());
                    case 2 -> feeder.progress();
                    case 3 -> lowWord(CryoConfig.VALUES.canisterFuel.get());
                    case 4 -> highWord(CryoConfig.VALUES.canisterFuel.get());
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {}

            @Override
            public int getCount() {
                return 5;
            }
        };
    }

    private static int lowWord(int value) {
        return value & 0xFFFF;
    }

    private static int highWord(int value) {
        return value >>> 16 & 0xFFFF;
    }

    private static int combineWords(int low, int high) {
        return low & 0xFFFF | (high & 0xFFFF) << 16;
    }
}