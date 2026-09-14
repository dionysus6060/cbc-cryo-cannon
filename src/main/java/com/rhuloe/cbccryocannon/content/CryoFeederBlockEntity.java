package com.rhuloe.cbccryocannon.content;

import com.rhuloe.cbccryocannon.config.CryoConfig;
import com.rhuloe.cbccryocannon.logic.FeederMath;
import com.rhuloe.cbccryocannon.registry.CryoRegistries;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;
import rbasamoyai.createbigcannons.cannons.big_cannons.IBigCannonBlockEntity;

public final class CryoFeederBlockEntity extends KineticBlockEntity implements MenuProvider {
    public static final int SHELL_SLOTS = 9;
    public static final int FUEL_SLOT = 9;
    private int fuel;
    private int progress;
    private BlockPos linkedBreech;
    private UUID linkId;
    private final ItemStackHandler inventory = new ItemStackHandler(10) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == FUEL_SLOT) return isCanister(stack);
            return stack.is(CryoRegistries.CRYOLITE_SHELL_ITEM.get());
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot < SHELL_SLOTS) syncModelState();
        }
    };

    public CryoFeederBlockEntity(BlockPos pos, BlockState state) {
        super(CryoRegistries.CRYO_FEEDER_ENTITY.get(), pos, state);
    }

    @Override
    public float calculateStressApplied() {
        return CryoConfig.VALUES.stressImpact.get().floatValue();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        int requiredStress = CryoConfig.VALUES.stressImpact.get() * CryoConfig.VALUES.minimumRpm.get();
        tooltip.add(Component.translatable("goggle.cbc_cryo_cannon.required_stress", requiredStress)
            .withStyle(ChatFormatting.GRAY));
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;
        syncModelState();
        absorbCanister();
        if (linkedBreech == null) {
            progress = 0;
            return;
        }
        if (level.getBlockEntity(linkedBreech) instanceof CryoBreechBlockEntity breech) {
            if (!CryoLinks.ensureStationaryIdentity(this, breech)) return;
            FeedTarget target = findStationaryTarget(breech);
            if (target != null) tryFeed(breech, target.cannon(), target.direction());
        }
    }

    public boolean tryFeedMounted(CryoBreechBlockEntity breech, IBigCannonBlockEntity target,
        Direction firingDirection) {
        return level != null && !level.isClientSide && linkedBreech != null && linkId != null
            && worldPosition.equals(breech.linkedFeeder())
            && Objects.equals(linkId, breech.linkId())
            && tryFeed(breech, target, firingDirection);
    }

    private boolean tryFeed(CryoBreechBlockEntity breech, IBigCannonBlockEntity target,
        Direction firingDirection) {
        if (Math.abs(getSpeed()) < CryoConfig.VALUES.minimumRpm.get()) return false;
        if (!breech.cannonBehavior().block().state().isAir()) {
            if (breech.cannonBehavior().block().state().is(CryoRegistries.CRYOLITE_SHELL.get())
                && target.cannonBehavior().block().state().isAir()) {
                StructureBlockInfo oldShell = breech.cannonBehavior().block();
                BlockState migratedState = oldShell.state().setValue(BlockStateProperties.FACING, firingDirection);
                target.cannonBehavior().loadBlock(new StructureBlockInfo(BlockPos.ZERO, migratedState, oldShell.nbt()));
                breech.cannonBehavior().removeBlock();
                markCannonChanged(breech);
                markCannonChanged(target);
                progress = 0;
                return true;
            }
            progress = 0;
            return false;
        }
        if (!target.cannonBehavior().block().state().isAir()) {
            progress = 0;
            return false;
        }
        int reloadTarget = FeederMath.reloadTicks(CryoConfig.VALUES.reloadTicks.get(), (int) Math.abs(getSpeed()),
            CryoConfig.VALUES.minimumRpm.get(), CryoConfig.VALUES.rpmAffectsReloadRate.get());
        if (reloadTarget == Integer.MAX_VALUE || !hasShell()
            || !FeederMath.canLoad(fuel, CryoConfig.VALUES.shotFuel.get())) return false;
        if (++progress >= reloadTarget) {
            BlockState shell = CryoRegistries.CRYOLITE_SHELL.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, firingDirection);
            target.cannonBehavior().loadBlock(new StructureBlockInfo(BlockPos.ZERO, shell, null));
            consumeShell();
            progress = 0;
            fuel -= CryoConfig.VALUES.shotFuel.get();
            markCannonChanged(target);
            setChanged();
            notifyUpdate();
            return true;
        }
        return false;
    }

    private FeedTarget findStationaryTarget(CryoBreechBlockEntity breech) {
        Direction facing = breech.getBlockState().getValue(BlockStateProperties.FACING);
        for (Direction direction : new Direction[] {facing, facing.getOpposite()}) {
            BlockEntity candidate = level.getBlockEntity(breech.getBlockPos().relative(direction));
            if (candidate instanceof IBigCannonBlockEntity cannon
                && (candidate.getBlockState().is(CryoRegistries.CRYO_CHAMBER.get())
                    || candidate.getBlockState().is(CryoRegistries.CRYO_BARREL.get()))) {
                return new FeedTarget(cannon, direction);
            }
        }
        return null;
    }

    private static void markCannonChanged(IBigCannonBlockEntity cannon) {
        if (cannon instanceof BlockEntity blockEntity) {
            blockEntity.setChanged();
            if (blockEntity instanceof com.simibubi.create.foundation.blockEntity.SmartBlockEntity smart) {
                smart.notifyUpdate();
            }
        }
    }

    private record FeedTarget(IBigCannonBlockEntity cannon, Direction direction) {}

    public ItemStackHandler inventory() {
        return inventory;
    }

    public int fuel() {
        return fuel;
    }

    public int progress() {
        return progress;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.cbc_cryo_cannon.cryo_feeder");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CryoFeederMenu(containerId, playerInventory, this);
    }

    public int comparatorValue() {
        int cap = CryoConfig.VALUES.canisterFuel.get();
        return Math.min(15, fuel * 15 / Math.max(1, cap));
    }

    public BlockPos linkedBreech() {
        return linkedBreech;
    }

    public void setLinkedBreech(BlockPos linkedBreech) {
        setLink(linkedBreech, linkedBreech == null ? null : linkId);
    }

    public void setLink(BlockPos linkedBreech, UUID linkId) {
        this.linkedBreech = linkedBreech;
        this.linkId = linkId;
        if (linkedBreech == null) progress = 0;
        syncModelState();
        setChanged();
        notifyUpdate();
    }

    public UUID linkId() {
        return linkId;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return linkedBreech == null ? super.getRenderBoundingBox() : AABB.INFINITE;
    }

    private boolean hasShell() {
        for (int slot = 0; slot < SHELL_SLOTS; slot++) if (!inventory.getStackInSlot(slot).isEmpty()) return true;
        return false;
    }

    private void syncModelState() {
        if (level == null || level.isClientSide || level.getBlockEntity(worldPosition) != this) return;
        BlockState current = getBlockState();
        if (!current.hasProperty(CryoFeederBlock.LINKED)
            || !current.hasProperty(CryoFeederBlock.SHELLS)) return;
        BlockState updated = current
            .setValue(CryoFeederBlock.LINKED, linkedBreech != null)
            .setValue(CryoFeederBlock.SHELLS, hasShell());
        if (updated != current) level.setBlock(worldPosition, updated, 3);
    }

    private void consumeShell() {
        for (int slot = 0; slot < SHELL_SLOTS; slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                inventory.extractItem(slot, 1, false);
                return;
            }
        }
    }

    private void absorbCanister() {
        if (fuel > 0) return;
        ItemStack canister = inventory.getStackInSlot(FUEL_SLOT);
        if (isCanister(canister)) {
            inventory.extractItem(FUEL_SLOT, 1, false);
            fuel = CryoConfig.VALUES.canisterFuel.get();
            setChanged();
            notifyUpdate();
        }
    }

    private static boolean isCanister(ItemStack stack) {
        return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem())
            .equals(ResourceLocation.parse("spore:ice_canister"));
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("Fuel", fuel);
        tag.putInt("Progress", progress);
        tag.put("Inventory", inventory.serializeNBT(registries));
        if (linkedBreech != null) tag.putLong("LinkedBreech", linkedBreech.asLong());
        if (linkId != null) tag.putUUID("LinkId", linkId);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        fuel = Math.clamp(tag.getInt("Fuel"), 0, CryoConfig.VALUES.canisterFuel.get());
        progress = tag.getInt("Progress");
        if (tag.contains("Inventory")) inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        linkedBreech = tag.contains("LinkedBreech") ? BlockPos.of(tag.getLong("LinkedBreech")) : null;
        linkId = tag.hasUUID("LinkId") ? tag.getUUID("LinkId") : null;
    }
}