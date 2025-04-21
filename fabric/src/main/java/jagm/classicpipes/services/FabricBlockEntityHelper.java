package jagm.classicpipes.services;

import jagm.classicpipes.ClassicPipes;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public class FabricBlockEntityHelper implements BlockEntityHelper{

    @Override
    public <T extends BlockEntity> BlockEntityType<T> registerBlockEntityType(String name, BiFunction<BlockPos, BlockState, T> blockEntitySupplier, Block... validBlocks) {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(ClassicPipes.MOD_ID, name),
                FabricBlockEntityTypeBuilder.create(blockEntitySupplier::apply, validBlocks).build()
        );
    }

}
