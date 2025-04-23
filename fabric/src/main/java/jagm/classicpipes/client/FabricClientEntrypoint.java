package jagm.classicpipes.client;

import jagm.classicpipes.ClassicPipes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

public class FabricClientEntrypoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClassicPipes.TRANSPARENT_BLOCKS.forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout()));
    }

}
