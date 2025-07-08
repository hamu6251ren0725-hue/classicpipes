package jagm.classicpipes.services;

import jagm.classicpipes.blockentity.AbstractPipeEntity;
import jagm.classicpipes.util.ItemInPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

public class ForgeBlockEntityHelper implements BlockEntityHelper {

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
        if (blockEntity instanceof AbstractPipeEntity) {
            return false;
        }
        if (blockEntity != null) {
            Optional<IItemHandler> itemHandlerOptional = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, face).resolve();
            if (itemHandlerOptional.isPresent()) {
                return itemHandlerOptional.get().getSlots() > 0;
            }
        }
        // Forge transfer API does not wrap sided containers without block entities, e.g. composters, so they must be handled separately.
        BlockState state = level.getBlockState(containerPos);
        if (state.getBlock() instanceof WorldlyContainerHolder containerHolder) {
            return containerHolder.getContainer(state, level, containerPos).getSlotsForFace(face).length > 0;
        }
        return false;
    }

    @Override
    public boolean handleItemInsertion(AbstractPipeEntity pipe, ServerLevel level, BlockPos pipePos, BlockState pipeState, ItemInPipe item) {
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
        if (blockEntity != null) {
            Optional<IItemHandler> itemHandlerOptional = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, face).resolve();
            if (itemHandlerOptional.isPresent()) {
                IItemHandler itemHandler = itemHandlerOptional.get();
                ItemStack stack = item.getStack();
                for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                    stack = itemHandler.insertItem(slot, stack, false);
                    if (stack.isEmpty()) {
                        return true;
                    }
                }
                item.setStack(stack);
            }
        }
        // Forge transfer API does not wrap sided containers without block entities, e.g. composters, so they must be handled separately.
        if (state.getBlock() instanceof WorldlyContainerHolder containerHolder) {
            WorldlyContainer container = containerHolder.getContainer(state, level, containerPos);
            ItemStack stack = HopperBlockEntity.addItem(null, container, item.getStack(), face);
            if (stack.isEmpty()) {
                return true;
            }
            item.setStack(stack);
        }
        item.resetProgress(item.getTargetDirection());
        pipe.routeItem(pipeState, item);
        return false;
    }

    @Override
    public boolean handleItemExtraction(AbstractPipeEntity pipe, BlockState pipeState, ServerLevel level, BlockPos containerPos, Direction face, int amount) {
        BlockEntity blockEntity = level.getBlockEntity(containerPos);
        if (blockEntity instanceof AbstractPipeEntity) {
            return false;
        }
        if (blockEntity != null) {
            Optional<IItemHandler> itemHandlerOptional = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, face).resolve();
            if (itemHandlerOptional.isPresent()) {
                IItemHandler itemHandler = itemHandlerOptional.get();
                for (int slot = itemHandler.getSlots() - 1; slot >= 0; slot--) {
                    ItemStack stack = itemHandler.extractItem(slot, amount, false);
                    if (!stack.isEmpty()) {
                        pipe.setItem(face.getOpposite().get3DDataValue(), stack);
                        return true;
                    }
                }
            }
        }
        // Forge transfer API does not wrap worldly containers without block entities, e.g. composters, so they must be handled separately.
        BlockState state = level.getBlockState(containerPos);
        if (state.getBlock() instanceof WorldlyContainerHolder containerHolder) {
            WorldlyContainer container = containerHolder.getContainer(state, level, containerPos);
            int[] slots = container.getSlotsForFace(face);
            for (int i = slots.length - 1; i >= 0; i--) {
                int slot = slots[i];
                ItemStack stack = container.getItem(slot);
                if (!stack.isEmpty() && container.canTakeItemThroughFace(slot, stack, face)) {
                    int count = stack.getCount();
                    if (HopperBlockEntity.addItem(container, pipe, container.removeItem(slot, amount), face.getOpposite()).isEmpty()) {
                        container.setChanged();
                        return true;
                    } else {
                        stack.setCount(count);
                        if (count == 1) {
                            container.setItem(slot, stack);
                        }
                    }
                }
            }
        }
        return false;
    }

}
