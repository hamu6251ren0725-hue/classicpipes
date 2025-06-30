package jagm.classicpipes.services;

import jagm.classicpipes.blockentity.AbstractPipeEntity;
import jagm.classicpipes.util.ItemInPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

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

    @Override
    public boolean canAccessContainer(Level level, BlockPos containerPos, Direction face) {
        BlockEntity blockEntity = level.getBlockEntity(containerPos);
        BlockState state = level.getBlockState(containerPos);
        if (blockEntity instanceof AbstractPipeEntity) {
            return false;
        }
        IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, containerPos, state, blockEntity, face);
        if (itemHandler != null) {
            return itemHandler.getSlots() > 0;
        }
        return false;
    }

    @Override
    public boolean handleItemInsertion(ServerLevel level, BlockPos pipePos, ItemInPipe item) {
        BlockPos containerPos = pipePos.relative(item.getTargetDirection());
        BlockEntity blockEntity = level.getBlockEntity(containerPos);
        if (blockEntity instanceof AbstractPipeEntity nextPipe) {
            item.resetProgress(item.getTargetDirection().getOpposite());
            nextPipe.insertPipeItem(level, item);
            level.sendBlockUpdated(containerPos, nextPipe.getBlockState(), nextPipe.getBlockState(), 2);
            return true;
        }
        Direction face = item.getTargetDirection().getOpposite();
        BlockState state = level.getBlockState(containerPos);
        IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, containerPos, state, blockEntity, face);
        if (itemHandler != null) {
            ItemStack stack = item.getStack();
            for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                stack = itemHandler.insertItem(slot, stack, false);
                if (stack.isEmpty()) {
                    return true;
                }
            }
            item.setStack(stack);
            item.resetProgress(item.getTargetDirection());
            return false;
        }
        item.resetProgress(item.getTargetDirection());
        return false;
    }

    @Override
    public boolean handleItemExtraction(AbstractPipeEntity pipe, ServerLevel level, BlockPos containerPos, Direction face, int amount) {
        BlockEntity blockEntity = level.getBlockEntity(containerPos);
        if (blockEntity instanceof AbstractPipeEntity) {
            return false;
        }
        BlockState state = level.getBlockState(containerPos);
        IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, containerPos, state, blockEntity, face);
        if (itemHandler != null) {
            for (int slot = itemHandler.getSlots() - 1; slot >= 0; slot--) {
                ItemStack stack = itemHandler.extractItem(slot, amount, false);
                if (!stack.isEmpty()) {
                    pipe.setItem(face.getOpposite().get3DDataValue(), stack);
                    return true;
                }
            }
        }
        return false;
    }

}
