package com.rhuloe.cbccryocannon.content;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlock;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlockItem;

public final class CryoCannonBlockItem<T extends Block & BigCannonBlock> extends BigCannonBlockItem<T> {
    public CryoCannonBlockItem(T block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        InteractionResult result = super.place(context);
        if (!context.getLevel().isClientSide && result.consumesAction()) {
            CryoCannonTubeBlock.refreshBarrelsAround(context.getLevel(), context.getClickedPos());
        }
        return result;
    }
}