package com.rhuloe.cbccryocannon.content;

import com.rhuloe.cbccryocannon.registry.CryoRegistries;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import rbasamoyai.createbigcannons.cannons.big_cannons.breeches.sliding_breech.SlidingBreechBlockEntity;

public final class CryoBreechBlockEntity extends SlidingBreechBlockEntity {
    private BlockPos linkedFeeder;
    private BlockPos linkAnchor;
    private UUID linkId;

    public CryoBreechBlockEntity(BlockEntityType<? extends CryoBreechBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean canLoadBlock(StructureBlockInfo info) {
        return info.state().is(CryoRegistries.CRYOLITE_SHELL.get()) && super.canLoadBlock(info);
    }

    public BlockPos linkedFeeder() {
        return linkedFeeder;
    }

    public void setLinkedFeeder(BlockPos linkedFeeder) {
        setLink(linkedFeeder, linkedFeeder == null ? null : linkId,
            linkedFeeder == null ? null : linkAnchor);
    }

    public void setLink(BlockPos linkedFeeder, UUID linkId) {
        setLink(linkedFeeder, linkId, linkAnchor);
    }

    public void setLink(BlockPos linkedFeeder, UUID linkId, BlockPos linkAnchor) {
        this.linkedFeeder = linkedFeeder;
        this.linkId = linkId;
        this.linkAnchor = linkAnchor;
        syncLinkedState();
        setChanged();
        notifyUpdate();
    }

    @Override
    public void initialize() {
        super.initialize();
        syncLinkedState();
    }

    public UUID linkId() {
        return linkId;
    }

    public BlockPos linkAnchor() {
        return linkAnchor;
    }

    private void syncLinkedState() {
        if (level == null || level.isClientSide || level.getBlockEntity(worldPosition) != this) return;
        BlockState current = getBlockState();
        if (!current.hasProperty(CryoBreechBlock.LINKED)
            || current.getValue(CryoBreechBlock.LINKED) == (linkedFeeder != null)) return;
        level.setBlock(worldPosition, current.setValue(CryoBreechBlock.LINKED, linkedFeeder != null), 3);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (linkedFeeder != null) tag.putLong("LinkedFeeder", linkedFeeder.asLong());
        if (linkAnchor != null) tag.putLong("LinkAnchor", linkAnchor.asLong());
        if (linkId != null) tag.putUUID("LinkId", linkId);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        linkedFeeder = tag.contains("LinkedFeeder") ? BlockPos.of(tag.getLong("LinkedFeeder")) : null;
        linkAnchor = tag.contains("LinkAnchor") ? BlockPos.of(tag.getLong("LinkAnchor")) : null;
        linkId = tag.hasUUID("LinkId") ? tag.getUUID("LinkId") : null;
    }
}