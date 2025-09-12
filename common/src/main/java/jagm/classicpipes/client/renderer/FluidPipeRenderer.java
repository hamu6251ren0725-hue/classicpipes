package jagm.classicpipes.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jagm.classicpipes.blockentity.FluidPipeEntity;
import jagm.classicpipes.services.Services;
import jagm.classicpipes.util.FluidInPipe;
import jagm.classicpipes.util.ItemInPipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class FluidPipeRenderer implements BlockEntityRenderer<FluidPipeEntity> {

    private final BlockEntityRendererProvider.Context context;

    public FluidPipeRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(FluidPipeEntity pipe, float partialTicks, PoseStack poses, MultiBufferSource bufferSource, int light, int overlay, Vec3 vec3) {
        if (!pipe.isEmpty()) {
            Map<Direction, Integer> amountPerSide = new HashMap<>();
            for (FluidInPipe fluidPacket : pipe.getContents()) {
                Direction direction = fluidPacket.getProgress() < ItemInPipe.HALFWAY ? fluidPacket.getFromDirection() : fluidPacket.getTargetDirection();
                if (amountPerSide.containsKey(direction)) {
                    amountPerSide.put(direction, amountPerSide.get(direction) + fluidPacket.getAmount());
                } else {
                    amountPerSide.put(direction, fluidPacket.getAmount());
                }
            }
            poses.pushPose();
            Matrix4f matrix = poses.last().pose();
            FluidRenderInfo info = Services.LOADER_SERVICE.getFluidRenderInfo(pipe.getFluid().defaultFluidState(), pipe.getLevel(), pipe.getBlockPos());
            TextureAtlasSprite fluidSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(info.texture());
            if (fluidSprite == null) {
                fluidSprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.WATER.defaultBlockState());
            }
            VertexConsumer vertexBuffer = bufferSource.getBuffer(RenderType.text(fluidSprite.atlasLocation()));
            float maxWidth = 0.0625F;
            for (Direction direction : amountPerSide.keySet()) {
                float width = Math.min(7.0F, amountPerSide.get(direction) * 8.0F / ((float) FluidPipeEntity.CAPACITY / 2)) / 16.0F;
                maxWidth = Math.max(width, maxWidth);
            }
            for (Direction direction : amountPerSide.keySet()) {
                float width = Math.min(7.0F, amountPerSide.get(direction) * 8.0F / ((float) FluidPipeEntity.CAPACITY / 2)) / 16.0F;
                float start = 0.5F - width / 2;
                float end = 0.5F + width / 2;
                switch (direction) {
                    case UP -> this.renderFluidCuboid(vertexBuffer, matrix, start, 0.5F + maxWidth / 2, start, end, 1.0F, end, fluidSprite, info.tint(), light, true, Direction.UP);
                    case DOWN -> this.renderFluidCuboid(vertexBuffer, matrix, start, 0.0F, start, end, 0.5F - maxWidth / 2, end, fluidSprite, info.tint(), light, true, Direction.DOWN);
                    case EAST -> this.renderFluidCuboid(vertexBuffer, matrix, 0.5F + maxWidth / 2, start, start, 1.0F, end, end, fluidSprite, info.tint(), light, true, Direction.WEST);
                    case WEST -> this.renderFluidCuboid(vertexBuffer, matrix, 0.0F, start, start, 0.5F - maxWidth / 2, end, end, fluidSprite, info.tint(), light, true, Direction.EAST);
                    case SOUTH -> this.renderFluidCuboid(vertexBuffer, matrix, start, start, 0.5F + maxWidth / 2, end, end, 1.0F, fluidSprite, info.tint(), light, true, Direction.NORTH);
                    case NORTH -> this.renderFluidCuboid(vertexBuffer, matrix, start, start, 0.0F, end, end, 0.5F - maxWidth / 2, fluidSprite, info.tint(), light, true, Direction.SOUTH);
                }
            }
            float start = 0.5F - maxWidth / 2;
            float end = 0.5F + maxWidth / 2;
            this.renderFluidCuboid(vertexBuffer, matrix, start, start, start, end, end, end, fluidSprite, info.tint(), light, false, Direction.DOWN);
            poses.popPose();
        }
    }

    public void renderFluidCuboid(VertexConsumer vertexBuffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, TextureAtlasSprite fluidSprite, int tint, int light, boolean shouldSkip, Direction skip) {
        // y-axis
        float u1 = fluidSprite.getU(x1);
        float u2 = fluidSprite.getU(x2);
        float v1 = fluidSprite.getV(z1);
        float v2 = fluidSprite.getV(z2);
        if (!shouldSkip || !skip.equals(Direction.UP)) {
            vertexBuffer.addVertex(matrix, x1, y1, z2).setColor(tint).setUv(u1, v2).setLight(light);
            vertexBuffer.addVertex(matrix, x1, y1, z1).setColor(tint).setUv(u1, v1).setLight(light);
            vertexBuffer.addVertex(matrix, x2, y1, z1).setColor(tint).setUv(u2, v1).setLight(light);
            vertexBuffer.addVertex(matrix, x2, y1, z2).setColor(tint).setUv(u2, v2).setLight(light);
        }
        if (!shouldSkip || !skip.equals(Direction.DOWN)) {
            vertexBuffer.addVertex(matrix, x1, y2, z1).setColor(tint).setUv(u1, v2).setLight(light);
            vertexBuffer.addVertex(matrix, x1, y2, z2).setColor(tint).setUv(u1, v1).setLight(light);
            vertexBuffer.addVertex(matrix, x2, y2, z2).setColor(tint).setUv(u2, v1).setLight(light);
            vertexBuffer.addVertex(matrix, x2, y2, z1).setColor(tint).setUv(u2, v2).setLight(light);
        }
        // x-axis
        u1 = fluidSprite.getU(y1);
        u2 = fluidSprite.getU(y2);
        v1 = fluidSprite.getV(z1);
        v2 = fluidSprite.getV(z2);
        if (!shouldSkip || !skip.equals(Direction.WEST)) {
            vertexBuffer.addVertex(matrix, x1, y2, z2).setColor(tint).setUv(u1, v2).setLight(light);
            vertexBuffer.addVertex(matrix, x1, y2, z1).setColor(tint).setUv(u1, v1).setLight(light);
            vertexBuffer.addVertex(matrix, x1, y1, z1).setColor(tint).setUv(u2, v1).setLight(light);
            vertexBuffer.addVertex(matrix, x1, y1, z2).setColor(tint).setUv(u2, v2).setLight(light);
        }
        if (!shouldSkip || !skip.equals(Direction.EAST)) {
            vertexBuffer.addVertex(matrix, x2, y2, z1).setColor(tint).setUv(u1, v2).setLight(light);
            vertexBuffer.addVertex(matrix, x2, y2, z2).setColor(tint).setUv(u1, v1).setLight(light);
            vertexBuffer.addVertex(matrix, x2, y1, z2).setColor(tint).setUv(u2, v1).setLight(light);
            vertexBuffer.addVertex(matrix, x2, y1, z1).setColor(tint).setUv(u2, v2).setLight(light);
        }
        // z-axis
        u1 = fluidSprite.getU(y1);
        u2 = fluidSprite.getU(y2);
        v1 = fluidSprite.getV(x1);
        v2 = fluidSprite.getV(x2);
        if (!shouldSkip || !skip.equals(Direction.NORTH)) {
            vertexBuffer.addVertex(matrix, x1, y2, z1).setColor(tint).setUv(u1, v2).setLight(light);
            vertexBuffer.addVertex(matrix, x2, y2, z1).setColor(tint).setUv(u1, v1).setLight(light);
            vertexBuffer.addVertex(matrix, x2, y1, z1).setColor(tint).setUv(u2, v1).setLight(light);
            vertexBuffer.addVertex(matrix, x1, y1, z1).setColor(tint).setUv(u2, v2).setLight(light);
        }
        if (!shouldSkip || !skip.equals(Direction.SOUTH)) {
            vertexBuffer.addVertex(matrix, x2, y2, z2).setColor(tint).setUv(u1, v2).setLight(light);
            vertexBuffer.addVertex(matrix, x1, y2, z2).setColor(tint).setUv(u1, v1).setLight(light);
            vertexBuffer.addVertex(matrix, x1, y1, z2).setColor(tint).setUv(u2, v1).setLight(light);
            vertexBuffer.addVertex(matrix, x2, y1, z2).setColor(tint).setUv(u2, v2).setLight(light);
        }
    }

}
