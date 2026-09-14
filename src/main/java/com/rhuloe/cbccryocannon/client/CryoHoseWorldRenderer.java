package com.rhuloe.cbccryocannon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rhuloe.cbccryocannon.content.CryoBreechBlockEntity;
import com.rhuloe.cbccryocannon.content.CryoFeederBlockEntity;
import com.rhuloe.cbccryocannon.registry.CryoRegistries;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import rbasamoyai.createbigcannons.cannon_control.contraption.MountedBigCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

public final class CryoHoseWorldRenderer {
    private static final ResourceLocation HOSE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
        "cbc_cryo_cannon", "textures/block/hose/coil.png");
    private static final RenderType HOSE_RENDER_TYPE = RenderType.entityCutout(HOSE_TEXTURE);
    private static final float HOSE_WIDTH = 0.375F + 0.1F / 16.0F;
    private static final double HOSE_END_OVERLAP = 0.5D / 16.0D;
    private static final Set<BlockPos> STATIONARY_FEEDERS = new HashSet<>();
    private static ClientLevel trackedLevel;

    private CryoHoseWorldRenderer() {}

    public static void trackStationaryFeeder(CryoFeederBlockEntity feeder) {
        if (!(feeder.getLevel() instanceof ClientLevel level)) return;
        resetForLevel(level);
        STATIONARY_FEEDERS.add(feeder.getBlockPos().immutable());
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) return;
        resetForLevel(level);

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();
        Vec3 camera = event.getCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        boolean rendered = false;

        Iterator<BlockPos> feederPositions = STATIONARY_FEEDERS.iterator();
        while (feederPositions.hasNext()) {
            BlockPos feederPos = feederPositions.next();
            BlockEntity blockEntity = level.getBlockEntity(feederPos);
            if (!(blockEntity instanceof CryoFeederBlockEntity feeder)) {
                if (level.isLoaded(feederPos)) feederPositions.remove();
                continue;
            }
            BlockPos breechPos = feeder.linkedBreech();
            if (breechPos == null
                || !(level.getBlockEntity(breechPos) instanceof CryoBreechBlockEntity breech)
                || feeder.linkId() == null || !Objects.equals(feeder.linkId(), breech.linkId())) continue;
            Vec3 start = Vec3.atBottomCenterOf(feederPos);
            Vec3 end = breechHosePoint(breechPos, breech.getBlockState());
            renderSegments(level, start, end, camera, poseStack, buffers, blockRenderer);
            rendered = true;
        }

        for (Entity rawEntity : level.entitiesForRendering()) {
            if (!(rawEntity instanceof PitchOrientedContraptionEntity entity)
                || !(entity.getContraption() instanceof MountedBigCannonContraption cannon)) continue;
            for (var entry : cannon.presentBlockEntities.entrySet()) {
                BlockEntity blockEntity = entry.getValue();
                if (!(blockEntity instanceof CryoBreechBlockEntity breech)
                    || breech.linkedFeeder() == null
                    || !(level.getBlockEntity(breech.linkedFeeder()) instanceof CryoFeederBlockEntity feeder)
                    || feeder.linkedBreech() == null || feeder.linkId() == null
                    || !Objects.equals(feeder.linkId(), breech.linkId())) continue;
                Vec3 start = Vec3.atBottomCenterOf(feeder.getBlockPos());
                Vec3 end = entity.toGlobalVector(
                    breechHosePoint(entry.getKey(), breech.getBlockState()), partialTick);
                renderSegments(level, start, end, camera, poseStack, buffers, blockRenderer);
                rendered = true;
            }
        }
        if (rendered) {
            buffers.endBatch(HOSE_RENDER_TYPE);
            buffers.endBatch(RenderType.solid());
        }
    }

    private static void resetForLevel(ClientLevel level) {
        if (trackedLevel == level) return;
        trackedLevel = level;
        STATIONARY_FEEDERS.clear();
    }

    private static void renderSegments(ClientLevel level, Vec3 start, Vec3 end, Vec3 camera,
        PoseStack poseStack, MultiBufferSource buffers, BlockRenderDispatcher blockRenderer) {
        Vec3 delta = end.subtract(start);
        if (delta.lengthSqr() < 0.001D) return;
        Vec3 overlap = delta.normalize().scale(HOSE_END_OVERLAP);
        Vec3 hoseStart = start.subtract(overlap);
        Vec3 hoseEnd = end.add(overlap);
        Vec3 hoseDelta = hoseEnd.subtract(hoseStart);
        renderHose(hoseStart.subtract(camera), hoseDelta, poseStack, buffers,
            LevelRenderer.getLightColor(level, BlockPos.containing(start.add(delta.scale(0.5D)))));
        renderDecoration(level, start.add(delta.scale(0.5D)), delta, camera, poseStack, buffers, blockRenderer);
    }

    private static Vec3 breechHosePoint(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        Direction facing = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);
        Vec3 towardOpening = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(2.0D / 16.0D);
        return Vec3.atCenterOf(pos).add(0, 8.0D / 16.0D, 0).add(towardOpening);
    }

    private static void renderHose(Vec3 relativeStart, Vec3 delta, PoseStack poseStack,
        MultiBufferSource buffers, int light) {
        VertexConsumer consumer = buffers.getBuffer(HOSE_RENDER_TYPE);
        Vector3f hoseVector = delta.toVector3f();
        float length = hoseVector.length();
        Vector3f direction = new Vector3f(hoseVector).normalize();
        Vector3f up = new Vector3f(0, 1, 0);
        Vector3f right = Math.abs(direction.x()) < 1.0E-6F && Math.abs(direction.z()) < 1.0E-6F
            ? new Vector3f(1, 0, 0) : new Vector3f(direction).cross(up).normalize();
        up = new Vector3f(right).cross(direction).normalize();

        float radius = HOSE_WIDTH * 0.5F;
        Vector3f p1 = new Vector3f(up).add(right).mul(radius);
        Vector3f p2 = new Vector3f(up).sub(right).mul(radius);
        Vector3f p3 = new Vector3f(up).negate().sub(right).mul(radius);
        Vector3f p4 = new Vector3f(up).negate().add(right).mul(radius);
        Vector3f e1 = new Vector3f(p1).add(hoseVector);
        Vector3f e2 = new Vector3f(p2).add(hoseVector);
        Vector3f e3 = new Vector3f(p3).add(hoseVector);
        Vector3f e4 = new Vector3f(p4).add(hoseVector);

        poseStack.pushPose();
        poseStack.translate(relativeStart.x, relativeStart.y, relativeStart.z);
        Matrix4f matrix = poseStack.last().pose();
        float uMin = 5.0F / 16.0F;
        float uMax = 11.0F / 16.0F;
        float vMax = length * (uMax - uMin) * 0.5F / HOSE_WIDTH;
        quad(consumer, matrix, p1, e1, e2, p2, uMin, uMax, 0, vMax, light);
        quad(consumer, matrix, p2, e2, e3, p3, uMin, uMax, 0, vMax, light);
        quad(consumer, matrix, p3, e3, e4, p4, uMin, uMax, 0, vMax, light);
        quad(consumer, matrix, p4, e4, e1, p1, uMin, uMax, 0, vMax, light);
        poseStack.popPose();
    }

    private static void renderDecoration(ClientLevel level, Vec3 midpoint, Vec3 delta, Vec3 camera,
        PoseStack poseStack, MultiBufferSource buffers,
        BlockRenderDispatcher blockRenderer) {
        poseStack.pushPose();
        poseStack.translate(midpoint.x - camera.x, midpoint.y - camera.y, midpoint.z - camera.z);
        poseStack.mulPose(new Quaternionf().rotationTo(new Vector3f(0, 1, 0), delta.toVector3f().normalize()));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        blockRenderer.renderSingleBlock(CryoRegistries.CRYO_HOSE_DECORATION.get().defaultBlockState(),
            poseStack, buffers, LevelRenderer.getLightColor(level, BlockPos.containing(midpoint)),
            OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix, Vector3f p1, Vector3f p2,
        Vector3f p3, Vector3f p4, float uMin, float uMax, float vMin, float vMax, int light) {
        vertex(consumer, matrix, p1, uMin, vMin, light);
        vertex(consumer, matrix, p2, uMin, vMax, light);
        vertex(consumer, matrix, p3, uMax, vMax, light);
        vertex(consumer, matrix, p4, uMax, vMin, light);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Vector3f position,
        float u, float v, int light) {
        consumer.addVertex(matrix, position.x(), position.y(), position.z())
            .setColor(255, 255, 255, 255)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(0, 1, 0);
    }
}