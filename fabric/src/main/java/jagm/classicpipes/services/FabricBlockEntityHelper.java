package jagm.classicpipes.services;

import com.google.common.base.Supplier;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public class FabricBlockEntityHelper implements BlockEntityHelper{

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> getBlockEntitySupplier(BiFunction<BlockPos, BlockState, T> blockEntitySupplier, Block... validBlocks) {
        return () -> FabricBlockEntityTypeBuilder.create(blockEntitySupplier::apply, validBlocks).build();
    }

}
