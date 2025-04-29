package jagm.classicpipes;

import jagm.classicpipes.util.MiscUtil;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class FabricEntrypoint implements ModInitializer {

    @Override
    public void onInitialize() {
        ClassicPipes.ITEMS.forEach((name, item) -> Registry.register(BuiltInRegistries.ITEM, MiscUtil.resourceLocation(name), item));
        ClassicPipes.BLOCKS.forEach((name, block) -> Registry.register(BuiltInRegistries.BLOCK, MiscUtil.resourceLocation(name), block));
        registerBlockEntity("wooden_pipe", ClassicPipes.WOODEN_PIPE_ENTITY);
        registerBlockEntity("golden_pipe", ClassicPipes.GOLDEN_PIPE_ENTITY);
        registerBlockEntity("copper_pipe", ClassicPipes.COPPER_PIPE_ENTITY);
        ClassicPipes.SOUNDS.forEach((name, soundEvent) -> Registry.register(BuiltInRegistries.SOUND_EVENT, MiscUtil.resourceLocation(name), soundEvent));
    }

    private static <T extends BlockEntity> void registerBlockEntity(String name, BlockEntityType<T> blockEntityType) {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, MiscUtil.resourceLocation(name), blockEntityType);
    }

}
