package jagm.classicpipes;

import jagm.classicpipes.client.PipeRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(ClassicPipes.MOD_ID)
public class ForgeEntrypoint {

    public ForgeEntrypoint(FMLJavaModLoadingContext context) {

    }

    @Mod.EventBusSubscriber(modid = ClassicPipes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventHandler {

        @SubscribeEvent
        public static void onRegister(RegisterEvent event) {
            event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
                ClassicPipes.BLOCKS.forEach(helper::register);
            });
            event.register(ForgeRegistries.Keys.ITEMS, helper -> {
                ClassicPipes.ITEMS.forEach(helper::register);
            });
            event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, helper -> {
                helper.register("wooden_pipe", ClassicPipes.WOODEN_PIPE_ENTITY);
                helper.register("golden_pipe", ClassicPipes.GOLDEN_PIPE_ENTITY);
                helper.register("copper_pipe", ClassicPipes.COPPER_PIPE_ENTITY);
                helper.register("iron_pipe", ClassicPipes.IRON_PIPE_ENTITY);
                helper.register("diamond_pipe", ClassicPipes.DIAMOND_PIPE_ENTITY);
            });
            event.register(ForgeRegistries.Keys.SOUND_EVENTS, helper -> {
                ClassicPipes.SOUNDS.forEach(helper::register);
            });
            event.register(Registries.CREATIVE_MODE_TAB, helper -> {
                helper.register(ClassicPipes.PIPES_TAB_KEY, ClassicPipes.PIPES_TAB);
            });
        }

    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ClassicPipes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEventHandler {

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ClassicPipes.WOODEN_PIPE_ENTITY, PipeRenderer::new);
            event.registerBlockEntityRenderer(ClassicPipes.GOLDEN_PIPE_ENTITY, PipeRenderer::new);
            event.registerBlockEntityRenderer(ClassicPipes.COPPER_PIPE_ENTITY, PipeRenderer::new);
            event.registerBlockEntityRenderer(ClassicPipes.IRON_PIPE_ENTITY, PipeRenderer::new);
            event.registerBlockEntityRenderer(ClassicPipes.DIAMOND_PIPE_ENTITY, PipeRenderer::new);
        }

        @SubscribeEvent
        public static void onFillCreativeTabs(BuildCreativeModeTabContentsEvent event) {
            if(event.getTabKey() == ClassicPipes.PIPES_TAB_KEY) {
                ClassicPipes.ITEMS.forEach((name, item) -> event.accept(item));
            }
        }

    }

}
