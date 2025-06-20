package jagm.classicpipes;

import jagm.classicpipes.client.PipeRenderer;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(ClassicPipes.MOD_ID)
public class NeoForgeEntrypoint {

    public NeoForgeEntrypoint(IEventBus eventBus) {

    }

    @EventBusSubscriber(modid = ClassicPipes.MOD_ID)
    public static class ModEventHandler {

        @SubscribeEvent
        public static void onRegister(RegisterEvent event) {
            event.register(Registries.BLOCK,helper -> {
                ClassicPipes.BLOCKS.forEach((name, block) -> helper.register(MiscUtil.resourceLocation(name), block));
            });
            event.register(Registries.ITEM, helper -> {
                ClassicPipes.ITEMS.forEach((name, item) -> helper.register(MiscUtil.resourceLocation(name), item));
            });
            event.register(Registries.BLOCK_ENTITY_TYPE, helper -> {
                helper.register(MiscUtil.resourceLocation("wooden_pipe"), ClassicPipes.WOODEN_PIPE_ENTITY);
                helper.register(MiscUtil.resourceLocation("golden_pipe"), ClassicPipes.GOLDEN_PIPE_ENTITY);
                helper.register(MiscUtil.resourceLocation("copper_pipe"), ClassicPipes.COPPER_PIPE_ENTITY);
                helper.register(MiscUtil.resourceLocation("iron_pipe"), ClassicPipes.IRON_PIPE_ENTITY);
            });
            event.register(Registries.SOUND_EVENT, helper -> {
                ClassicPipes.SOUNDS.forEach((name, soundEvent) -> helper.register(MiscUtil.resourceLocation(name), soundEvent));
            });
            event.register(Registries.CREATIVE_MODE_TAB, helper -> {
                helper.register(ClassicPipes.PIPES_TAB_KEY, ClassicPipes.PIPES_TAB);
            });
        }

    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = ClassicPipes.MOD_ID)
    public static class ClientModEventHandler {

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ClassicPipes.WOODEN_PIPE_ENTITY, PipeRenderer::new);
            event.registerBlockEntityRenderer(ClassicPipes.GOLDEN_PIPE_ENTITY, PipeRenderer::new);
            event.registerBlockEntityRenderer(ClassicPipes.COPPER_PIPE_ENTITY, PipeRenderer::new);
            event.registerBlockEntityRenderer(ClassicPipes.IRON_PIPE_ENTITY, PipeRenderer::new);
        }

        @SubscribeEvent
        public static void onFillCreativeTabs(BuildCreativeModeTabContentsEvent event) {
            if(event.getTabKey() == ClassicPipes.PIPES_TAB_KEY) {
                ClassicPipes.ITEMS.forEach((name, item) -> event.accept(item));
            }
        }

    }

}
