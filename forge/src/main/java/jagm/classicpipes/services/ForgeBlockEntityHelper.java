package jagm.classicpipes.services;

import com.google.common.base.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.function.BiFunction;

public class ForgeBlockEntityHelper implements BlockEntityHelper {

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<? extends BlockEntity>> getBlockEntitySupplier(BiFunction<BlockPos, BlockState, T> blockEntitySupplier, Block... validBlocks) {
        return () -> new BlockEntityType<>(blockEntitySupplier::apply, Set.of(validBlocks));
    }

}
