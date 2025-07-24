package jagm.classicpipes;

import jagm.classicpipes.client.PipeRenderer;
import jagm.classicpipes.client.screen.DiamondPipeScreen;
import jagm.classicpipes.client.screen.ProviderPipeScreen;
import jagm.classicpipes.client.screen.RoutingPipeScreen;
import jagm.classicpipes.network.ServerBoundDefaultRoutePayload;
import jagm.classicpipes.network.ServerBoundLeaveOnePayload;
import jagm.classicpipes.network.ServerBoundMatchComponentsPayload;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(ClassicPipes.MOD_ID)
@SuppressWarnings("unused")
public class NeoForgeEntrypoint {

    public NeoForgeEntrypoint(IEventBus eventBus) {

    }

    @EventBusSubscriber(modid = ClassicPipes.MOD_ID)
    public static class ModEventHandler {

        @SubscribeEvent
        public static void onRegister(RegisterEvent event) {

            event.register(Registries.BLOCK, helper -> ClassicPipes.BLOCKS.forEach((name, block) -> helper.register(MiscUtil.resourceLocation(name), block)));
            event.register(Registries.ITEM, helper -> ClassicPipes.ITEMS.forEach((name, item) -> helper.register(MiscUtil.resourceLocation(name), item)));
            event.register(Registries.SOUND_EVENT, helper -> ClassicPipes.SOUNDS.forEach((name, soundEvent) -> helper.register(MiscUtil.resourceLocation(name), soundEvent)));
            event.register(Registries.CREATIVE_MODE_TAB, helper -> helper.register(ClassicPipes.PIPES_TAB_KEY, ClassicPipes.PIPES_TAB));

            event.register(Registries.BLOCK_ENTITY_TYPE, helper -> {
                helper.register(MiscUtil.resourceLocation("basic_pipe"), ClassicPipes.BASIC_PIPE_ENTITY);
                helper.register(MiscUtil.resourceLocation("golden_pipe"), ClassicPipes.GOLDEN_PIPE_ENTITY);
                helper.register(MiscUtil.resourceLocation("copper_pipe"), ClassicPipes.COPPER_PIPE_ENTITY);
                helper.register(MiscUtil.resourceLocation("iron_pipe"), ClassicPipes.IRON_PIPE_ENTITY);
                helper.register(MiscUtil.resourceLocation("diamond_pipe"), ClassicPipes.DIAMOND_PIPE_ENTITY);
                helper.register(MiscUtil.resourceLocation("flint_pipe"), ClassicPipes.FLINT_PIPE_ENTITY);
                helper.register(MiscUtil.resourceLocation("lapis_pipe"), ClassicPipes.LAPIS_PIPE_ENTITY);
                helper.register(MiscUtil.resourceLocation("obsidian_pipe"), ClassicPipes.OBSIDIAN_PIPE_ENTITY);
                helper.register(MiscUtil.resourceLocation("routing_pipe"), ClassicPipes.ROUTING_PIPE_ENTITY);
                helper.register(MiscUtil.resourceLocation("provider_pipe"), ClassicPipes.PROVIDER_PIPE_ENTITY);
                helper.register(MiscUtil.resourceLocation("request_pipe"), ClassicPipes.REQUEST_PIPE_ENTITY);
            });

            event.register(Registries.MENU, helper -> {
                helper.register(MiscUtil.resourceLocation("diamond_pipe"), ClassicPipes.DIAMOND_PIPE_MENU);
                helper.register(MiscUtil.resourceLocation("routing_pipe"), ClassicPipes.ROUTING_PIPE_MENU);
                helper.register(MiscUtil.resourceLocation("provider_pipe"), ClassicPipes.PROVIDER_PIPE_MENU);
            });

        }

        @SubscribeEvent
        public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");
            registrar.playToServer(ServerBoundMatchComponentsPayload.TYPE, ServerBoundMatchComponentsPayload.STREAM_CODEC, (payload, context) -> payload.handle(context.player()));
            registrar.playToServer(ServerBoundDefaultRoutePayload.TYPE, ServerBoundDefaultRoutePayload.STREAM_CODEC, (payload, context) -> payload.handle(context.player()));
            registrar.playToServer(ServerBoundLeaveOnePayload.TYPE, ServerBoundLeaveOnePayload.STREAM_CODEC, (payload, context) -> payload.handle(context.player()));
        }

    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = ClassicPipes.MOD_ID)
    public static class ClientModEventHandler {

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ClassicPipes.BASIC_PIPE_ENTITY, PipeRenderer::new);
            event.registerBlockEntityRenderer(ClassicPipes.GOLDEN_PIPE_ENTITY, PipeRenderer::new);
            event.registerBlockEntityRenderer(ClassicPipes.COPPER_PIPE_ENTITY, PipeRenderer::new);
            event.registerBlockEntityRenderer(ClassicPipes.IRON_PIPE_ENTITY, PipeRenderer::new);
            event.registerBlockEntityRenderer(ClassicPipes.DIAMOND_PIPE_ENTITY, PipeRenderer::new);
            event.registerBlockEntityRenderer(ClassicPipes.FLINT_PIPE_ENTITY, PipeRenderer::new);
            event.registerBlockEntityRenderer(ClassicPipes.LAPIS_PIPE_ENTITY, PipeRenderer::new);
            event.registerBlockEntityRenderer(ClassicPipes.OBSIDIAN_PIPE_ENTITY, PipeRenderer::new);
            event.registerBlockEntityRenderer(ClassicPipes.ROUTING_PIPE_ENTITY, PipeRenderer::new);
            event.registerBlockEntityRenderer(ClassicPipes.PROVIDER_PIPE_ENTITY, PipeRenderer::new);
            event.registerBlockEntityRenderer(ClassicPipes.REQUEST_PIPE_ENTITY, PipeRenderer::new);
        }

        @SubscribeEvent
        public static void onFillCreativeTabs(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == ClassicPipes.PIPES_TAB_KEY) {
                ClassicPipes.ITEMS.forEach((name, item) -> event.accept(item));
            }
        }

        @SubscribeEvent
        public static void onRegisterScreens(RegisterMenuScreensEvent event) {
            event.register(ClassicPipes.DIAMOND_PIPE_MENU, DiamondPipeScreen::new);
            event.register(ClassicPipes.ROUTING_PIPE_MENU, RoutingPipeScreen::new);
            event.register(ClassicPipes.PROVIDER_PIPE_MENU, ProviderPipeScreen::new);
        }

    }

}
