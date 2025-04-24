package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
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

public abstract class TransportPipeEntity extends BlockEntity implements WorldlyContainer {

    protected final List<ItemInPipe> contents;

    public TransportPipeEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.contents = new ArrayList<>();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (blockEntity instanceof TransportPipeEntity pipe) {
            if (!pipe.isEmpty()) {
                ListIterator<ItemInPipe> iterator = pipe.contents.listIterator();
                boolean hasChanged = false;
                while (iterator.hasNext()) {
                    hasChanged = pipe.tickItem(iterator.next(), level, pos, state, iterator);
                }
                if (hasChanged) {
                    pipe.setChanged();
                    if (level instanceof ServerLevel serverLevel){
                        serverLevel.sendBlockUpdated(pos, state, state, 2);
                    }
                }
            }
        }
    }

    private void debug (String message, ItemInPipe item) {
        if (this.getLevel() != null){
            ClassicPipes.LOGGER.info("({}) {} {}x {}.", this.getLevel().isClientSide ? "Client" : "Server", message, item.getStack().getCount(), item.getStack().getDisplayName().getString());
        }
    }

    public boolean tickItem(ItemInPipe item, Level level, BlockPos pos, BlockState state, ListIterator<ItemInPipe> iterator) {
        boolean hasChanged = false;
        item.move(this.getTargetSpeed(), this.getAcceleration());
        if (item.isEjecting() && item.getProgress() >= ItemInPipe.HALFWAY) {
            hasChanged = true;
            iterator.remove();
            debug("Ejecting", item);
            if (level instanceof ServerLevel serverLevel) {
                item.drop(serverLevel, pos);
            }
        } else if (item.getProgress() >= ItemInPipe.PIPE_LENGTH) {
            // The item should be removed even if it remains in this pipe because it will be re-added when routeItem is called.
            iterator.remove();
            hasChanged = true;
            debug("Handling exit of", item);
            if (level instanceof ServerLevel serverLevel) {
                Container container = TransportPipeBlock.getBlockContainer(level, pos.relative(item.getTargetDirection()));
                if (container == null) {
                    // Bounce the item backwards.
                    debug("Bouncing from null container", item);
                    item.resetProgress(item.getTargetDirection());
                    List<ItemInPipe> toRoute = this.routeItem(state, item);
                    toRoute.forEach(iterator::add);
                } else if (container instanceof TransportPipeEntity nextPipe) {
                    // Pass the item to the next pipe.
                    debug("Passing to next pipe", item);
                    item.resetProgress(item.getTargetDirection().getOpposite());
                    List<ItemInPipe> toRoute = nextPipe.routeItem(item);
                    nextPipe.contents.addAll(toRoute);
                    nextPipe.setChanged();
                    serverLevel.sendBlockUpdated(nextPipe.worldPosition, nextPipe.getBlockState(), nextPipe.getBlockState(), 2);
                } else {
                    // HopperBlockEntity.addItem returns the stack of items that was not able to be added to the container.
                    debug("Inserting into container", item);
                    ItemStack stack = HopperBlockEntity.addItem(this, container, item.getStack(), item.getTargetDirection().getOpposite());
                    if (!stack.isEmpty()) {
                        // Bounce the remaining items backwards.
                        debug("Rejected by container", item);
                        item.setStack(stack);
                        item.resetProgress(item.getTargetDirection());
                        List<ItemInPipe> toRoute = this.routeItem(state, item);
                        toRoute.forEach(iterator::add);
                    }
                }
            }

        }
        return hasChanged;
    }

    // routeItem should never return ItemInPipe instances with empty item stacks, and should always explicitly set each ItemInPipe to be ejecting or not ejecting.
    public List<ItemInPipe> routeItem(BlockState state, ItemInPipe item) {
        debug("Routing", item);
        List<ItemInPipe> outputs = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (this.isPipeConnected(state, direction) && !direction.equals(item.getFromDirection())) {
                // Create an empty copy of the item for each valid output direction.
                outputs.add(new ItemInPipe(item.getStack().copyWithCount(0), item.getSpeed(), item.getProgress(), item.getFromDirection(), direction, false));
            }
        }
        if(outputs.isEmpty()){
            // Set the item to eject because there were no valid output directions.
            debug("Setting to eject", item);
            item.setTargetDirection(item.getFromDirection().getOpposite());
            item.setEjecting(true);
            outputs.add(item);
        } else {
            // Randomly distribute the items in a stack to the outputs.
            for(int i = 0; i < item.getStack().getCount(); i++){
                if (this.level != null) {
                    outputs.get(this.level.getRandom().nextInt(outputs.size())).getStack().grow(1);
                }
            }
            // Get rid of ItemInPipe instances with empty stacks.
            outputs.removeIf(outputItem -> outputItem.getStack().isEmpty());
        }
        return outputs;
    }

    public final List<ItemInPipe> routeItem(ItemInPipe item) {
        return routeItem(this.getBlockState(), item);
    }

    protected final boolean isPipeConnected(BlockState state, Direction direction) {
        return state.getValue(TransportPipeBlock.PROPERTY_BY_DIRECTION.get(direction));
    }

    public final void update(Level level, BlockState state, BlockPos pos, Direction direction) {
        /*if (!this.isEmpty() && !state.equals(this.getBlockState())) {
            ListIterator<ItemInPipe> iterator = this.contents.listIterator();
            while (iterator.hasNext()) {
                ItemInPipe item = iterator.next();
                if (!this.isPipeConnected(state, direction)) {
                    if (direction.equals(item.getFromDirection()) && item.getProgress() < ItemInPipe.HALFWAY) {
                        // A connection was severed while the item was entering the pipe from that direction.
                        debug("Dropping from severed entry connection", item);
                        iterator.remove();
                        if (level instanceof ServerLevel serverLevel) {
                            item.drop(serverLevel, pos);
                        }
                    } else if (direction.equals(item.getTargetDirection())) {
                        iterator.remove();
                        if (item.getProgress() >= ItemInPipe.HALFWAY) {
                            // A connection was severed while the item was leaving the pipe in that direction.
                            debug("Dropping from severed target connection", item);
                            if (level instanceof ServerLevel serverLevel) {
                                item.drop(serverLevel, pos);
                            }
                        } else {
                            // A connection was severed while the item was entering from a different direction, so its target needs to be recalculated.
                            debug("Rerouting due to severed connection", item);
                            if (level instanceof ServerLevel) {
                                this.routeItem(state, item).forEach(iterator::add);
                            }
                        }
                    }
                } else if (item.getProgress() < ItemInPipe.HALFWAY) {
                    // A new connection was established while the item was entering the pipe, so its target needs to be recalculated in case it should go that way.
                    debug("Rerouting due to new connection", item);
                    iterator.remove();
                    if (level instanceof ServerLevel) {
                        this.routeItem(state, item).forEach(iterator::add);
                    }
                }
                // If the item is already leaving the pipe then it doesn't care about new or missing connections except the one it's in.
            }
            this.setChanged();
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendBlockUpdated(pos, state, state, 2);
            }
        }*/
    }

    public void dropItems(ServerLevel serverLevel, BlockPos pos) {
        for (ItemInPipe item : this.contents) {
            item.drop(serverLevel, pos);
        }
    }

    public int getTargetSpeed() {
        return ItemInPipe.DEFAULT_SPEED;
    }

    public int getAcceleration() {
        return 1;
    }

    public List<ItemInPipe> getContents() {
        return this.contents;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        super.loadAdditional(tag, levelRegistry);
        ListTag items = tag.getListOrEmpty("items");
        for (int i = 0; i < items.size(); i++) {
            CompoundTag itemTag = items.getCompoundOrEmpty(i);
            ItemInPipe item = ItemInPipe.parse(itemTag, levelRegistry);
            if (item != null) {
                if (!item.getStack().isEmpty()) {
                    contents.add(item);
                }
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
        CompoundTag tag = super.getUpdateTag(levelRegistry);
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
    public void setItem(int slot, ItemStack stack) {
        debug("Received into pipe", new ItemInPipe(stack, Direction.UP, Direction.UP));
        Direction direction = Direction.from3DDataValue(slot);
        if (level instanceof ServerLevel serverLevel) {
            List<ItemInPipe> toRoute = this.routeItem(new ItemInPipe(stack, direction, direction.getOpposite()));
            this.contents.addAll(toRoute);
            serverLevel.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
        }
        this.setChanged();
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
