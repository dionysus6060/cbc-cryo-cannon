package com.rhuloe.cbccryocannon.content;

import com.rhuloe.cbccryocannon.registry.CryoRegistries;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CryoFeederBlock extends DirectionalKineticBlock implements IBE<CryoFeederBlockEntity> {
    public static final BooleanProperty LINKED = BooleanProperty.create("linked");
    public static final BooleanProperty SHELLS = BooleanProperty.create("shells");
    private static final VoxelShape SHAPE = Shapes.or(
        Shapes.block(), Block.box(3, -3, 3, 13, 0, 13));

    public CryoFeederBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LINKED, false).setValue(SHELLS, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        Direction facing = state.getValue(FACING);
        return facing.getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
    }

    @Override
    public boolean hasShaftTowards(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LINKED, SHELLS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return CryoWrenchLinking.useOnEndpoint(context);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
        Player player, InteractionHand hand, BlockHitResult hit) {
        if (AllItems.WRENCH.isIn(stack)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        BlockEntity entity = level.getBlockEntity(pos);
        if (!(entity instanceof CryoFeederBlockEntity feeder)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        for (int slot = 0; slot <= CryoFeederBlockEntity.FUEL_SLOT; slot++) {
            ItemStack remainder = feeder.inventory().insertItem(slot, stack, true);
            if (remainder.getCount() != stack.getCount()) {
                if (!level.isClientSide) {
                    ItemStack insertedRemainder = feeder.inventory().insertItem(slot, stack.copy(), false);
                    stack.setCount(insertedRemainder.getCount());
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
        BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CryoFeederBlockEntity feeder)) return InteractionResult.PASS;
        if (!level.isClientSide) player.openMenu(feeder, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        return entity instanceof CryoFeederBlockEntity feeder ? feeder.comparatorValue() : 0;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!level.isClientSide && !moving && !state.is(newState.getBlock())) {
            CryoLinks.detach(level, pos);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public Class<CryoFeederBlockEntity> getBlockEntityClass() {
        return CryoFeederBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CryoFeederBlockEntity> getBlockEntityType() {
        return CryoRegistries.CRYO_FEEDER_ENTITY.get();
    }
}