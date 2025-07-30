package jagm.classicpipes.services;

import jagm.classicpipes.blockentity.AbstractPipeEntity;
import jagm.classicpipes.util.ItemInPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.commons.lang3.function.TriFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public class NeoForgeService implements LoaderService {

    @Override
    public <B extends BlockEntity> BlockEntityType<B> createBlockEntityType(BiFunction<BlockPos, BlockState, B> blockEntitySupplier, Block... validBlocks) {
        return new BlockEntityType<>(blockEntitySupplier::apply, Set.of(validBlocks));
    }

    @Override
    public <M extends AbstractContainerMenu, D> MenuType<M> createMenuType(TriFunction<Integer, Inventory, D, M> menuSupplier, StreamCodec<RegistryFriendlyByteBuf, D> codec) {
        return IMenuTypeExtension.create((id, inventory, buffer) -> menuSupplier.apply(id, inventory, codec.decode(buffer)));
    }

    @Override
    public <D> void openMenu(ServerPlayer player, MenuProvider menuProvider, D payload, StreamCodec<RegistryFriendlyByteBuf, D> codec) {
        player.openMenu(menuProvider, buffer -> codec.encode(buffer, payload));
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    @Override
    public void sendToClient(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public boolean canAccessContainer(Level level, BlockPos containerPos, Direction face) {
        BlockEntity blockEntity = level.getBlockEntity(containerPos);
        if (blockEntity instanceof AbstractPipeEntity) {
            return false;
        }
        BlockState state = level.getBlockState(containerPos);
        IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, containerPos, state, blockEntity, face);
        if (itemHandler != null) {
            return itemHandler.getSlots() > 0;
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

    @Override
    public boolean extractSpecificItem(AbstractPipeEntity pipe, ServerLevel level, BlockPos containerPos, Direction face, ItemStack stack) {
        ItemStack target = stack.copy();
        IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, containerPos, face);
        if (itemHandler != null) {
            for (int slot = itemHandler.getSlots() - 1; slot >= 0; slot--) {
                if (ItemStack.isSameItemSameComponents(stack, itemHandler.getStackInSlot(slot))) {
                    ItemStack extracted = itemHandler.extractItem(slot, target.getCount(), false);
                    if (!extracted.isEmpty()) {
                        target.shrink(extracted.getCount());
                        pipe.setItem(face.getOpposite().get3DDataValue(), extracted);
                        if (target.isEmpty()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<ItemStack> getExtractableItems(ServerLevel level, BlockPos pos, Direction face) {
        IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, face);
        if (itemHandler != null) {
            List<ItemStack> stacks = new ArrayList<>();
            for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                ItemStack slotStack = itemHandler.extractItem(slot, itemHandler.getSlotLimit(slot), true);
                if (slotStack.isEmpty()) {
                    continue;
                }
                boolean matched = false;
                for (ItemStack stack : stacks) {
                    if (ItemStack.isSameItemSameComponents(stack, slotStack)) {
                        stack.setCount(stack.getCount() + slotStack.getCount());
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    stacks.add(slotStack);
                }
            }
            return stacks;
        }
        return List.of();
    }

    @Override
    public String getModName(String modId) {
        return ModList.get().getModContainerById(modId).map(ModContainer::getModInfo).map(IModInfo::getDisplayName).orElse(modId);
    }

}
