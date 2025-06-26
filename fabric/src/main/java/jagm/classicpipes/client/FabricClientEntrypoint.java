package jagm.classicpipes.client;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.client.screen.DiamondPipeScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public class FabricClientEntrypoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClassicPipes.TRANSPARENT_BLOCKS.forEach(block -> BlockRenderLayerMap.putBlock(block, ChunkSectionLayer.CUTOUT));
        BlockEntityRenderers.register(ClassicPipes.WOODEN_PIPE_ENTITY, PipeRenderer::new);
        BlockEntityRenderers.register(ClassicPipes.GOLDEN_PIPE_ENTITY, PipeRenderer::new);
        BlockEntityRenderers.register(ClassicPipes.COPPER_PIPE_ENTITY, PipeRenderer::new);
        BlockEntityRenderers.register(ClassicPipes.IRON_PIPE_ENTITY, PipeRenderer::new);
        BlockEntityRenderers.register(ClassicPipes.DIAMOND_PIPE_ENTITY, PipeRenderer::new);
        MenuScreens.register(ClassicPipes.DIAMOND_PIPE_MENU, DiamondPipeScreen::new);
    }

}
