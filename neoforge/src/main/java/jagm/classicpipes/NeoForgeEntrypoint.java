package jagm.classicpipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(ClassicPipes.MOD_ID)
public class NeoForgeEntrypoint {

    public NeoForgeEntrypoint(IEventBus eventBus) {

    }

    @EventBusSubscriber(modid = ClassicPipes.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEventHandler {

        @SubscribeEvent
        public static void onRegister(RegisterEvent event) {
            event.register(Registries.BLOCK,helper -> {
                ClassicPipes.BLOCKS.forEach((name, blockSupplier) -> helper.register(ResourceLocation.fromNamespaceAndPath(ClassicPipes.MOD_ID, name), blockSupplier.get()));
            });
            event.register(Registries.ITEM, helper -> {
                ClassicPipes.ITEMS.forEach((name, itemSupplier) -> helper.register(ResourceLocation.fromNamespaceAndPath(ClassicPipes.MOD_ID, name), itemSupplier.get()));
            });
            event.register(Registries.BLOCK_ENTITY_TYPE, helper -> {
                helper.register(ResourceLocation.fromNamespaceAndPath(ClassicPipes.MOD_ID, "wooden_pipe"), ClassicPipes.WOODEN_PIPE_ENTITY.get());
            });
        }

    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = ClassicPipes.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ClientModEventHandler {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            //ClassicPipes.TRANSPARENT_BLOCKS.forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout()));
        }

    }

}
