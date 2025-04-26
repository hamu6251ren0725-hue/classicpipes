package jagm.classicpipes.blockentity;

import jagm.classicpipes.block.TransportPipeBlock;
import jagm.classicpipes.util.ItemInPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public abstract class AbstractPipeEntity extends BlockEntity implements WorldlyContainer {

    protected final List<ItemInPipe> contents;
    protected final List<ItemInPipe> queued;

    public AbstractPipeEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.contents = new ArrayList<>();
        this.queued = new ArrayList<>();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (blockEntity instanceof AbstractPipeEntity pipe) {
            if (!pipe.isEmpty()) {
                if (level instanceof ServerLevel serverLevel) {
                    pipe.tickServer(serverLevel, pos, state);
                    pipe.addQueuedItems();
                } else {
                    pipe.tickClient(level, pos, state);
                }
                pipe.setChanged();
            }
        }
    }

    public void tickServer(ServerLevel level, BlockPos pos, BlockState state) {
        ListIterator<ItemInPipe> iterator = this.contents.listIterator();
        while (iterator.hasNext()) {
            ItemInPipe item = iterator.next();
            item.move(this.getTargetSpeed(), this.getAcceleration());
            if (item.isEjecting() && item.getProgress() >= ItemInPipe.HALFWAY) {
                iterator.remove();
                item.drop(level, pos);
                level.sendBlockUpdated(pos, state, state, 2);
            } else if (item.getProgress() >= ItemInPipe.PIPE_LENGTH) {
                Container container = TransportPipeBlock.getBlockContainer(level, pos.relative(item.getTargetDirection()));
                if (container == null) {
                    // Bounce the item backwards.
                    item.resetProgress(item.getTargetDirection());
                    this.routeItem(state, item);
                } else if (container instanceof AbstractPipeEntity nextPipe) {
                    // Pass the item to the next pipe.
                    item.resetProgress(item.getTargetDirection().getOpposite());
                    nextPipe.queued.add(item);
                    nextPipe.routeItem(item);
                    nextPipe.addQueuedItems();
                    iterator.remove();
                    level.sendBlockUpdated(nextPipe.worldPosition, nextPipe.getBlockState(), nextPipe.getBlockState(), 2);
                    nextPipe.setChanged();
                } else {
                    // HopperBlockEntity.addItem returns the stack of items that was not able to be added to the container.
                    ItemStack stack = HopperBlockEntity.addItem(this, container, item.getStack(), item.getTargetDirection().getOpposite());
                    if (!stack.isEmpty()) {
                        // Bounce the remaining items backwards.
                        item.setStack(stack);
                        item.resetProgress(item.getTargetDirection());
                        this.routeItem(state, item);
                    } else {
                        iterator.remove();
                    }
                }
                level.sendBlockUpdated(pos, state, state, 2);
            }
        }
    }

    public void tickClient(Level level, BlockPos pos, BlockState state) {
        for (ItemInPipe item : this.contents) {
            item.move(this.getTargetSpeed(), this.getAcceleration());
        }
    }

    protected void addQueuedItems() {
        this.contents.addAll(this.queued);
        this.queued.clear();
    }

    public abstract void routeItem(BlockState state, ItemInPipe item);

    public final void routeItem(ItemInPipe item) {
        routeItem(this.getBlockState(), item);
    }

    protected final boolean isPipeConnected(BlockState state, Direction direction) {
        return state.getValue(TransportPipeBlock.PROPERTY_BY_DIRECTION.get(direction));
    }

    public final void update(ServerLevel level, BlockState state, BlockPos pos, Direction direction, boolean wasConnected) {
        if (!this.isEmpty()) {
            ListIterator<ItemInPipe> iterator = this.contents.listIterator();
            while (iterator.hasNext()) {
                ItemInPipe item = iterator.next();
                if (!wasConnected) {
                    this.routeItem(state, item);
                } else {
                    if (item.getFromDirection() == direction && item.getProgress() < ItemInPipe.HALFWAY) {
                        iterator.remove();
                        item.drop(level, pos);
                    } else if (item.getTargetDirection() == direction) {
                        if (item.getProgress() < ItemInPipe.HALFWAY) {
                            this.routeItem(state, item);
                        } else {
                            iterator.remove();
                            item.drop(level, pos);
                        }

                    }
                }
            }
            this.addQueuedItems();
            this.setChanged();
            level.sendBlockUpdated(pos, state, state, 2);
        }
    }

    public void dropItems(ServerLevel serverLevel, BlockPos pos) {
        for (ItemInPipe item : this.contents) {
            item.drop(serverLevel, pos);
        }
    }

    public abstract int getTargetSpeed();

    public abstract int getAcceleration();

    public List<ItemInPipe> getContents() {
        return this.contents;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (level instanceof ServerLevel serverLevel && !stack.isEmpty()) {
            Direction direction = Direction.from3DDataValue(slot);
            ItemInPipe item = new ItemInPipe(stack, direction, direction.getOpposite());
            this.queued.add(item);
            this.routeItem(item);
            this.addQueuedItems();
            serverLevel.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
        }
        this.setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        this.clearContent();
        super.loadAdditional(tag, levelRegistry);
        ListTag items = tag.getListOrEmpty("items");
        for (int i = 0; i < items.size(); i++) {
            CompoundTag itemTag = items.getCompoundOrEmpty(i);
            ItemInPipe item = ItemInPipe.parse(itemTag, levelRegistry);
            if (!item.getStack().isEmpty()) {
                contents.add(item);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        super.saveAdditional(tag, levelRegistry);
        ListTag items = new ListTag();
        for (ItemInPipe item : this.contents) {
            if (!item.getStack().isEmpty()) {
                items.add(item.save(levelRegistry));
            }
        }
        tag.put("items", items);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider levelRegistry) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, levelRegistry);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void clearContent() {
        this.contents.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] {direction.get3DDataValue()};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        if (direction != null) {
            return slot == direction.get3DDataValue() && this.canPlaceItem(slot, stack);
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public int getContainerSize() {
        // Pipes have a fake slot for each of the six directions. These slots always appear to be empty.
        return 6;
    }

    @Override
    public boolean isEmpty() {
        return this.contents.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return this.isPipeConnected(this.getBlockState(), Direction.from3DDataValue(slot));
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return false;
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return new ItemStackIterator(this.contents);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    public static class ItemStackIterator implements Iterator<ItemStack> {

        private final List<ItemInPipe> contents;
        private int index;

        public ItemStackIterator(List<ItemInPipe> contents) {
            this.contents = contents;
        }

        @Override
        public boolean hasNext() {
            return this.index < this.contents.size();
        }

        @Override
        public ItemStack next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            } else {
                return this.contents.get(this.index++).getStack();
            }
        }

    }

}
