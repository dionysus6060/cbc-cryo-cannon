package com.rhuloe.cbccryocannon.content;

import com.rhuloe.cbccryocannon.registry.CryoRegistries;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import rbasamoyai.createbigcannons.cannon_control.contraption.MountedBigCannonContraption;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlock;
import rbasamoyai.createbigcannons.cannons.big_cannons.cannon_end.BigCannonEnd;
import rbasamoyai.createbigcannons.cannons.big_cannons.material.BigCannonMaterial;
import rbasamoyai.createbigcannons.crafting.casting.CannonCastShape;

public final class CryoBreechBlock extends DirectionalAxisKineticBlock
    implements IBE<CryoBreechBlockEntity>, BigCannonBlock {
    public static final BooleanProperty LINKED = BooleanProperty.create("linked");
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
        Shapes.block(), Block.box(3, 16, 5, 13, 20, 15));
    private static final VoxelShape SOUTH_SHAPE = Shapes.or(
        Shapes.block(), Block.box(3, 16, 1, 13, 20, 11));
    private static final VoxelShape WEST_SHAPE = Shapes.or(
        Shapes.block(), Block.box(5, 16, 3, 15, 20, 13));
    private static final VoxelShape EAST_SHAPE = Shapes.or(
        Shapes.block(), Block.box(1, 16, 3, 11, 20, 13));

    public CryoBreechBlock(Properties properties) {
        super(properties.pushReaction(PushReaction.BLOCK));
        registerDefaultState(defaultBlockState().setValue(LINKED, false));
    }

    @Override
    public BigCannonMaterial getCannonMaterial() {
        return CryoCannonMaterials.CRYO;
    }

    @Override
    public CannonCastShape getCannonShape() {
        return CannonCastShape.SLIDING_BREECH;
    }

    @Override
    public Direction getFacing(BlockState state) {
        return state.getValue(FACING);
    }

    @Override
    public BigCannonEnd getOpeningType(@Nullable Level level, BlockState state, BlockPos pos) {
        return level != null && level.getBlockEntity(pos) instanceof CryoBreechBlockEntity breech
            ? breech.getOpeningType() : BigCannonEnd.OPEN;
    }

    @Override
    public BigCannonEnd getOpeningType(MountedBigCannonContraption contraption, BlockState state, BlockPos pos) {
        return contraption.presentBlockEntities.get(pos) instanceof CryoBreechBlockEntity breech
            ? breech.getOpeningType() : BigCannonEnd.OPEN;
    }

    @Override
    public BigCannonEnd getDefaultOpeningType() {
        return BigCannonEnd.CLOSED;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getNearestLookingDirection().getOpposite();
        Direction horizontal = context.getHorizontalDirection();
        return defaultBlockState().setValue(FACING, facing)
            .setValue(AXIS_ALONG_FIRST_COORDINATE, horizontal.getAxis() == Direction.Axis.X);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LINKED);
    }

    @Override
    public boolean isComplete(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(LINKED)) return Shapes.block();
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> Shapes.block();
        };
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return CryoWrenchLinking.useOnEndpoint(context);
    }

    @Override
    public Class<CryoBreechBlockEntity> getBlockEntityClass() {
        return CryoBreechBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CryoBreechBlockEntity> getBlockEntityType() {
        return CryoRegistries.CRYO_BREECH_ENTITY.get();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            if (!moving) CryoLinks.detach(level, pos);
            onRemoveCannon(state, level, pos, newState, moving);
            CryoCannonTubeBlock.refreshBarrelsAround(level, pos);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public void weldBlock(Level level, BlockState state, BlockPos pos, Direction direction) {
        BigCannonBlock.super.weldBlock(level, state, pos, direction);
        if (!level.isClientSide) CryoCannonTubeBlock.refreshBarrelsAround(level, pos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) playerWillDestroyBigCannon(level, pos, state, player);
        return super.playerWillDestroy(level, pos, state, player);
    }
}