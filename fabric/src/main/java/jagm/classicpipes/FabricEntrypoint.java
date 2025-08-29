package jagm.classicpipes;

import jagm.classicpipes.network.*;
import jagm.classicpipes.util.MiscUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

@SuppressWarnings("unused")
public class FabricEntrypoint implements ModInitializer {

    @Override
    public void onInitialize() {

        ClassicPipes.ITEMS.forEach((name, item) -> Registry.register(BuiltInRegistries.ITEM, MiscUtil.resourceLocation(name), item));
        ClassicPipes.BLOCKS.forEach((name, block) -> Registry.register(BuiltInRegistries.BLOCK, MiscUtil.resourceLocation(name), block));
        ClassicPipes.SOUNDS.forEach((name, soundEvent) -> Registry.register(BuiltInRegistries.SOUND_EVENT, MiscUtil.resourceLocation(name), soundEvent));
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ClassicPipes.PIPES_TAB_KEY, ClassicPipes.PIPES_TAB);

        registerBlockEntity("basic_pipe", ClassicPipes.BASIC_PIPE_ENTITY);
        registerBlockEntity("golden_pipe", ClassicPipes.GOLDEN_PIPE_ENTITY);
        registerBlockEntity("copper_pipe", ClassicPipes.COPPER_PIPE_ENTITY);
        registerBlockEntity("iron_pipe", ClassicPipes.IRON_PIPE_ENTITY);
        registerBlockEntity("diamond_pipe", ClassicPipes.DIAMOND_PIPE_ENTITY);
        registerBlockEntity("flint_pipe", ClassicPipes.FLINT_PIPE_ENTITY);
        registerBlockEntity("lapis_pipe", ClassicPipes.LAPIS_PIPE_ENTITY);
        registerBlockEntity("obsidian_pipe", ClassicPipes.OBSIDIAN_PIPE_ENTITY);
        registerBlockEntity("bone_pipe", ClassicPipes.BONE_PIPE_ENTITY);
        registerBlockEntity("routing_pipe", ClassicPipes.ROUTING_PIPE_ENTITY);
        registerBlockEntity("provider_pipe", ClassicPipes.PROVIDER_PIPE_ENTITY);
        registerBlockEntity("request_pipe", ClassicPipes.REQUEST_PIPE_ENTITY);
        registerBlockEntity("stocking_pipe", ClassicPipes.STOCKING_PIPE_ENTITY);
        registerBlockEntity("matching_pipe", ClassicPipes.MATCHING_PIPE_ENTITY);
        registerBlockEntity("storage_pipe", ClassicPipes.STORAGE_PIPE_ENTITY);
        registerBlockEntity("recipe_pipe", ClassicPipes.RECIPE_PIPE_ENTITY);

        registerMenu("diamond_pipe", ClassicPipes.DIAMOND_PIPE_MENU);
        registerMenu("routing_pipe", ClassicPipes.ROUTING_PIPE_MENU);
        registerMenu("provider_pipe", ClassicPipes.PROVIDER_PIPE_MENU);
        registerMenu("request", ClassicPipes.REQUEST_MENU);
        registerMenu("stocking_pipe", ClassicPipes.STOCKING_PIPE_MENU);
        registerMenu("matching_pipe", ClassicPipes.MATCHING_PIPE_MENU);
        registerMenu("storage_pipe", ClassicPipes.STORAGE_PIPE_MENU);
        registerMenu("recipe_pipe", ClassicPipes.RECIPE_PIPE_MENU);

        ItemGroupEvents.modifyEntriesEvent(ClassicPipes.PIPES_TAB_KEY).register(tab -> ClassicPipes.ITEMS.forEach((name, item) -> tab.accept(item)));

        registerServerPayload(ServerBoundMatchComponentsPayload.TYPE, ServerBoundMatchComponentsPayload.STREAM_CODEC);
        registerServerPayload(ServerBoundDefaultRoutePayload.TYPE, ServerBoundDefaultRoutePayload.STREAM_CODEC);
        registerServerPayload(ServerBoundLeaveOnePayload.TYPE, ServerBoundLeaveOnePayload.STREAM_CODEC);
        registerServerPayload(ServerBoundSortingModePayload.TYPE, ServerBoundSortingModePayload.STREAM_CODEC);
        registerServerPayload(ServerBoundRequestPayload.TYPE, ServerBoundRequestPayload.STREAM_CODEC);
        registerServerPayload(ServerBoundActiveStockingPayload.TYPE, ServerBoundActiveStockingPayload.STREAM_CODEC);
        registerServerPayload(ServerBoundSlotDirectionPayload.TYPE, ServerBoundSlotDirectionPayload.STREAM_CODEC);

        PayloadTypeRegistry.playS2C().register(ClientBoundItemListPayload.TYPE, ClientBoundItemListPayload.STREAM_CODEC);

    }

    private static <T extends BlockEntity> void registerBlockEntity(String name, BlockEntityType<T> blockEntityType) {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, MiscUtil.resourceLocation(name), blockEntityType);
    }

    private static <T extends AbstractContainerMenu> void registerMenu(String name, MenuType<T> menuType) {
        Registry.register(BuiltInRegistries.MENU, MiscUtil.resourceLocation(name), menuType);
    }

    private static <T extends SelfHandler> void registerServerPayload(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.playC2S().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> payload.handle(context.player()));
    }

}
