package jagm.classicpipes;

import jagm.classicpipes.network.DefaultRoutePayload;
import jagm.classicpipes.network.MatchComponentsPayload;
import jagm.classicpipes.util.MiscUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class FabricEntrypoint implements ModInitializer {

    @Override
    public void onInitialize() {

        ClassicPipes.ITEMS.forEach((name, item) -> Registry.register(BuiltInRegistries.ITEM, MiscUtil.resourceLocation(name), item));
        ClassicPipes.BLOCKS.forEach((name, block) -> Registry.register(BuiltInRegistries.BLOCK, MiscUtil.resourceLocation(name), block));
        ClassicPipes.SOUNDS.forEach((name, soundEvent) -> Registry.register(BuiltInRegistries.SOUND_EVENT, MiscUtil.resourceLocation(name), soundEvent));

        registerBlockEntity("basic_pipe", ClassicPipes.BASIC_PIPE_ENTITY);
        registerBlockEntity("golden_pipe", ClassicPipes.GOLDEN_PIPE_ENTITY);
        registerBlockEntity("copper_pipe", ClassicPipes.COPPER_PIPE_ENTITY);
        registerBlockEntity("iron_pipe", ClassicPipes.IRON_PIPE_ENTITY);
        registerBlockEntity("diamond_pipe", ClassicPipes.DIAMOND_PIPE_ENTITY);
        registerBlockEntity("flint_pipe", ClassicPipes.FLINT_PIPE_ENTITY);
        registerBlockEntity("lapis_pipe", ClassicPipes.LAPIS_PIPE_ENTITY);
        registerBlockEntity("obsidian_pipe", ClassicPipes.OBSIDIAN_PIPE_ENTITY);
        registerBlockEntity("netherite_pipe", ClassicPipes.NETHERITE_BASIC_PIPE_ENTITY);

        registerMenu("diamond_pipe", ClassicPipes.DIAMOND_PIPE_MENU);
        registerMenu("netherite_pipe", ClassicPipes.NETHERITE_BASIC_PIPE_MENU);

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ClassicPipes.PIPES_TAB_KEY, ClassicPipes.PIPES_TAB);
        ItemGroupEvents.modifyEntriesEvent(ClassicPipes.PIPES_TAB_KEY).register(tab -> ClassicPipes.ITEMS.forEach((name, item) -> tab.accept(item)));

        PayloadTypeRegistry.playC2S().register(MatchComponentsPayload.TYPE, MatchComponentsPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(MatchComponentsPayload.TYPE, (payload, context) -> payload.handle(context.player()));
        PayloadTypeRegistry.playC2S().register(DefaultRoutePayload.TYPE, DefaultRoutePayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DefaultRoutePayload.TYPE, (payload, context) -> payload.handle(context.player()));

    }

    private static <T extends BlockEntity> void registerBlockEntity(String name, BlockEntityType<T> blockEntityType) {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, MiscUtil.resourceLocation(name), blockEntityType);
    }

    private static <T extends AbstractContainerMenu> void registerMenu(String name, MenuType<T> menuType) {
        Registry.register(BuiltInRegistries.MENU, MiscUtil.resourceLocation(name), menuType);
    }

}
