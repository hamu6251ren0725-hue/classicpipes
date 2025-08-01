package jagm.classicpipes.services;

import jagm.classicpipes.block.AbstractPipeBlock;
import jagm.classicpipes.blockentity.AbstractPipeEntity;
import jagm.classicpipes.util.ItemInPipe;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.function.TriFunction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

public class FabricService implements LoaderService {

    @Override
    public <B extends BlockEntity> BlockEntityType<B> createBlockEntityType(BiFunction<BlockPos, BlockState, B> blockEntitySupplier, Block... validBlocks) {
        return FabricBlockEntityTypeBuilder.create(blockEntitySupplier::apply, validBlocks).build();
    }

    @Override
    public <M extends AbstractContainerMenu, D> MenuType<M> createMenuType(TriFunction<Integer, Inventory, D, M> menuSupplier, StreamCodec<RegistryFriendlyByteBuf, D> codec) {
        return new ExtendedScreenHandlerType<>(menuSupplier::apply, codec);
    }

    @Override
    public <D> void openMenu(ServerPlayer player, MenuProvider menuProvider, D payload, StreamCodec<RegistryFriendlyByteBuf, D> codec) {
        player.openMenu(new ExtendedScreenHandlerFactory<D>() {

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                return menuProvider.createMenu(id, inventory, player);
            }

            @Override
            public Component getDisplayName() {
                return menuProvider.getDisplayName();
            }

            @Override
            public D getScreenOpeningData(ServerPlayer serverPlayer) {
                return payload;
            }

        });
    }

    @Override
    public boolean canAccessContainer(Level level, BlockPos containerPos, Direction face) {
        BlockState state = level.getBlockState(containerPos);
        if (state.getBlock() instanceof AbstractPipeBlock) {
            return false;
        }
        Storage<ItemVariant> itemHandler = ItemStorage.SIDED.find(level, containerPos, face);
        if (itemHandler != null) {
            return itemHandler.supportsExtraction() || itemHandler.supportsInsertion();
        }
        return false;
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public void sendToClient(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
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
        Storage<ItemVariant> itemHandler = ItemStorage.SIDED.find(level, containerPos, face);
        if (itemHandler != null) {
            int count = item.getStack().getCount();
            int inserted;
            try (Transaction transaction = Transaction.openOuter()) {
                inserted = (int) itemHandler.insert(ItemVariant.of(item.getStack()), count, transaction);
                transaction.commit();
            }
            if (inserted >= count) {
                return true;
            }
            item.setStack(item.getStack().copyWithCount(count - inserted));
        }
        item.resetProgress(item.getTargetDirection());
        pipe.routeItem(pipeState, item);
        return false;
    }

    @Override
    public boolean handleItemExtraction(AbstractPipeEntity pipe, BlockState pipeState, ServerLevel level, BlockPos containerPos, Direction face, int amount) {
        BlockState state = level.getBlockState(containerPos);
        if (state.getBlock() instanceof AbstractPipeBlock) {
            return false;
        }
        Storage<ItemVariant> itemHandler = ItemStorage.SIDED.find(level, containerPos, face);
        if (itemHandler != null) {
            List<StorageView<ItemVariant>> itemViewList = new ArrayList<>();
            itemHandler.nonEmptyIterator().forEachRemaining(itemViewList::add);
            ItemStack stack = ItemStack.EMPTY;
            try (Transaction transaction = Transaction.openOuter()) {
                for (int i = itemViewList.size() - 1; i >= 0; i--) {
                    StorageView<ItemVariant> itemView = itemViewList.get(i);
                    // Must get resource here, otherwise it might return empty after the extraction.
                    ItemVariant resource = itemView.getResource();
                    int extracted = (int) itemView.extract(itemView.getResource(), amount, transaction);
                    if (extracted > 0) {
                        stack = resource.toStack(extracted);
                        transaction.commit();
                        break;
                    }
                }
            }
            if (!stack.isEmpty()) {
                pipe.setItem(face.getOpposite().get3DDataValue(), stack);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ItemStack> getExtractableItems(ServerLevel level, BlockPos pos, Direction face) {
        Storage<ItemVariant> itemHandler = ItemStorage.SIDED.find(level, pos, face);
        if (itemHandler != null) {
            List<ItemStack> stacks = new ArrayList<>();
            Iterator<StorageView<ItemVariant>> iterator = itemHandler.nonEmptyIterator();
            while (iterator.hasNext()) {
                StorageView<ItemVariant> itemView = iterator.next();
                boolean matched = false;
                for (ItemStack stack : stacks) {
                    if (itemView.getResource().matches(stack)) {
                        stack.setCount(stack.getCount() + (int) itemView.getAmount());
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    stacks.add(itemView.getResource().toStack((int) itemView.getAmount()));
                }
            }
            return stacks;
        }
        return List.of();
    }

    public List<ItemStack> getContainerItems(ServerLevel level, BlockPos pos, Direction face) {
        Storage<ItemVariant> itemHandler = ItemStorage.SIDED.find(level, pos, face);
        if (itemHandler != null) {
            List<ItemStack> stacks = new ArrayList<>();
            Iterator<StorageView<ItemVariant>> iterator = itemHandler.nonEmptyIterator();
            while (iterator.hasNext()) {
                StorageView<ItemVariant> itemView = iterator.next();
                boolean matched = false;
                for (ItemStack stack : stacks) {
                    if (itemView.getResource().matches(stack)) {
                        stack.setCount(stack.getCount() + (int) itemView.getAmount());
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    stacks.add(itemView.getResource().toStack((int) itemView.getAmount()));
                }
            }
            return stacks;
        }
        return List.of();
    }

    @Override
    public boolean extractSpecificItem(AbstractPipeEntity pipe, ServerLevel level, BlockPos containerPos, Direction face, ItemStack stack) {
        Storage<ItemVariant> itemHandler = ItemStorage.SIDED.find(level, containerPos, face);
        boolean success = false;
        if (itemHandler != null) {
            try (Transaction transaction = Transaction.openOuter()) {
                int extracted = (int) itemHandler.extract(ItemVariant.of(stack), stack.getCount(), transaction);
                transaction.commit();
                if (extracted > 0) {
                    int itemsLeft = extracted;
                    while (itemsLeft > 0) {
                        int removed = Math.min(stack.getMaxStackSize(), itemsLeft);
                        pipe.setItem(face.getOpposite().get3DDataValue(), stack.copyWithCount(removed));
                        itemsLeft -= removed;
                    }
                    if (extracted == stack.getCount()) {
                        success = true;
                    }
                }
            }
        }
        return success;
    }

    @Override
    public String getModName(String modId) {
        return FabricLoader.getInstance().getModContainer(modId).map(ModContainer::getMetadata).map(ModMetadata::getName).orElse(modId);
    }

}
