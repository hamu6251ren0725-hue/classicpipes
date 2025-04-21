package jagm.classicpipes;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class FabricEntrypoint implements ModInitializer {

    @Override
    public void onInitialize() {
        ClassicPipes.ITEMS.forEach((name, itemSupplier) -> Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ClassicPipes.MOD_ID, name), itemSupplier.get()));
        ClassicPipes.BLOCKS.forEach((name, blockSupplier) -> Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(ClassicPipes.MOD_ID, name), blockSupplier.get()));
    }

}
