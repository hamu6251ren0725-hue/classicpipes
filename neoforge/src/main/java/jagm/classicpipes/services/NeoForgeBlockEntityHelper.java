package jagm.classicpipes.services;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.function.BiFunction;

public class NeoForgeBlockEntityHelper implements BlockEntityHelper{

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> blockEntitySupplier, Block... validBlocks) {
        return new BlockEntityType<>(blockEntitySupplier::apply, Set.of(validBlocks));
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createMenuType(BiFunction<Integer, Inventory, T> menuSupplier, FeatureFlagSet featureFlags) {
        return new MenuType<>(menuSupplier::apply, featureFlags);
    }

}
