package jagm.classicpipes;

import com.google.common.base.Supplier;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class FabricEntrypoint implements ModInitializer {

    @Override
    public void onInitialize() {
        ClassicPipes.ITEMS.forEach((name, itemSupplier) -> Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ClassicPipes.MOD_ID, name), itemSupplier.get()));
        ClassicPipes.BLOCKS.forEach((name, blockSupplier) -> Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(ClassicPipes.MOD_ID, name), blockSupplier.get()));
        registerBlockEntity("wooden_pipe", ClassicPipes.WOODEN_PIPE_ENTITY);
    }

    private static <T extends BlockEntity> void registerBlockEntity(String name, Supplier<BlockEntityType<T>> supplier) {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ClassicPipes.MOD_ID, name), supplier.get());
    }

}
