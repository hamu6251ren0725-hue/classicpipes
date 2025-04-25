package jagm.classicpipes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import jagm.classicpipes.blockentity.TransportPipeEntity;
import jagm.classicpipes.util.ItemInPipe;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

public class TransportPipeRenderer implements BlockEntityRenderer<TransportPipeEntity> {

    private final BlockEntityRendererProvider.Context context;

    public TransportPipeRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(TransportPipeEntity pipe, float partialTicks, PoseStack poses, MultiBufferSource bufferSource, int light, int overlay, Vec3 vec3) {
        if (!pipe.isEmpty()){
            for (ItemInPipe item : pipe.getContents()) {
                Direction direction = item.getProgress() < ItemInPipe.HALFWAY ? item.getFromDirection() : item.getTargetDirection();
                poses.pushPose();
                poses.translate(item.getRenderPosition(partialTicks));
                poses.scale(0.4375F, 0.4375F, 0.4375F);
                if (direction.equals(Direction.EAST) || direction.equals(Direction.WEST)) {
                    poses.mulPose(Axis.YP.rotationDegrees(90.0F));
                } else if (direction.equals(Direction.UP) || direction.equals(Direction.DOWN)) {
                    poses.mulPose(Axis.XP.rotationDegrees(90.0F));
                }
                context.getItemRenderer().renderStatic(item.getStack(), ItemDisplayContext.FIXED, light, overlay, poses, bufferSource, pipe.getLevel(), 0);
                poses.popPose();
            }
        }
    }

}
