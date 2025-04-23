package jagm.classicpipes;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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
                ClassicPipes.BLOCKS.forEach((name, blockSupplier) -> helper.register(name, blockSupplier.get()));
            });
            event.register(ForgeRegistries.Keys.ITEMS, helper -> {
                ClassicPipes.ITEMS.forEach((name, itemSupplier) -> helper.register(name, itemSupplier.get()));
            });
            event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, helper -> {
                ClassicPipes.BLOCK_ENTITIES.forEach((name, blockEntitySupplier) -> helper.register(name, blockEntitySupplier.get()));
            });
        }

    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ClassicPipes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEventHandler {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            //ClassicPipes.TRANSPARENT_BLOCKS.forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout()));
        }

    }

}
