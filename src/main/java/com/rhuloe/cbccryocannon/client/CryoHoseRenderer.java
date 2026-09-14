package com.rhuloe.cbccryocannon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rhuloe.cbccryocannon.content.CryoFeederBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public final class CryoHoseRenderer extends KineticBlockEntityRenderer<CryoFeederBlockEntity> {
    public CryoHoseRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(CryoFeederBlockEntity feeder, float partialTick, PoseStack poseStack,
        MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BlockState shaft = shaft(getRotationAxisOf(feeder));
        renderRotatingKineticBlock(feeder, shaft, poseStack, buffers.getBuffer(RenderType.solid()), packedLight);
        CryoHoseWorldRenderer.trackStationaryFeeder(feeder);
    }

    @Override
    public boolean shouldRenderOffScreen(CryoFeederBlockEntity blockEntity) {
        return true;
    }
}