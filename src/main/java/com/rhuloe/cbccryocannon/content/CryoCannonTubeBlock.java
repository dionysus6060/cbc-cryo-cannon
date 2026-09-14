package com.rhuloe.cbccryocannon.content;

import com.rhuloe.cbccryocannon.registry.CryoRegistries;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlock;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlockEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.IBigCannonBlockEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonTubeBlock;
import rbasamoyai.createbigcannons.crafting.casting.CannonCastShape;

public final class CryoCannonTubeBlock extends BigCannonTubeBlock {
    public static final EnumProperty<Segment> SEGMENT = EnumProperty.create("segment", Segment.class);
    private final boolean tracksBarrelTopology;

    public CryoCannonTubeBlock(BlockBehaviour.Properties properties, Supplier<CannonCastShape> shape,
        VoxelShape voxelShape, boolean tracksBarrelTopology) {
        super(properties, CryoCannonMaterials.CRYO, shape, voxelShape);
        this.tracksBarrelTopology = tracksBarrelTopology;
        registerDefaultState(defaultBlockState().setValue(SEGMENT, Segment.SINGLE));
    }

    @Override
    public BlockEntityType<? extends BigCannonBlockEntity> getBlockEntityType() {
        return CryoRegistries.CRYO_CANNON_ENTITY.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SEGMENT);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        boolean removed = !state.is(newState.getBlock());
        super.onRemove(state, level, pos, newState, moving);
        if (removed && !level.isClientSide) refreshBarrelsAround(level, pos);
    }

    @Override
    public void weldBlock(Level level, BlockState state, BlockPos pos, Direction direction) {
        super.weldBlock(level, state, pos, direction);
        if (!level.isClientSide) refreshBarrelsAround(level, pos);
    }

    public static void refreshBarrelsAround(Level level, BlockPos center) {
        refreshBarrel(level, center);
        for (Direction direction : Direction.values()) {
            refreshBarrel(level, center.relative(direction));
        }
    }

    static void refreshBarrel(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CryoCannonTubeBlock tube)
            || !tube.tracksBarrelTopology
            || !(level.getBlockEntity(pos) instanceof IBigCannonBlockEntity cannon)) return;

        Direction facing = tube.getFacing(state);
        boolean connectedFacing = cannon.cannonBehavior().isConnectedTo(facing);
        boolean connectedOpposite = cannon.cannonBehavior().isConnectedTo(facing.getOpposite());
        Segment segment = Segment.fromConnections(connectedFacing, connectedOpposite);
        if (state.getValue(SEGMENT) != segment) {
            level.setBlock(pos, state.setValue(SEGMENT, segment), Block.UPDATE_CLIENTS);
        }
    }

    public enum Segment implements StringRepresentable {
        SINGLE("single"),
        END_FACING("end_facing"),
        END_OPPOSITE("end_opposite"),
        MIDDLE("middle");

        private final String serializedName;

        Segment(String serializedName) {
            this.serializedName = serializedName;
        }

        public static Segment fromConnections(boolean connectedFacing, boolean connectedOpposite) {
            if (connectedFacing && connectedOpposite) return MIDDLE;
            if (connectedFacing) return END_OPPOSITE;
            if (connectedOpposite) return END_FACING;
            return SINGLE;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}