package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.block.AbstractPipeBlock;
import jagm.classicpipes.block.RoutingPipeBlock;
import jagm.classicpipes.services.Services;
import jagm.classicpipes.util.ItemInPipe;
import jagm.classicpipes.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public abstract class AbstractPipeEntity extends BlockEntity implements WorldlyContainer {

    protected final List<ItemInPipe> contents;
    protected final List<ItemInPipe> queued;
    public final Map<Direction, Tuple<BlockPos, Integer>> logistics;
    private boolean logisticsInitialised = false;
    private final Map<ItemInPipe, Long> tickAdded;

    public AbstractPipeEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.contents = new ArrayList<>();
        this.queued = new ArrayList<>();
        this.logistics = new HashMap<>();
        this.tickAdded = new HashMap<>();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (blockEntity instanceof AbstractPipeEntity pipe) {
            if (level instanceof ServerLevel serverLevel) {
                pipe.tickServer(serverLevel, pos, state);
            } else {
                pipe.tickClient(level, pos);
            }
        }
    }

    public void tickServer(ServerLevel level, BlockPos pos, BlockState state) {
        boolean sendBlockUpdate = false;
        if (!this.logisticsInitialised) {
            this.initialiseLogistics(level, state, pos); // sets changed
            this.logisticsInitialised = true;
            sendBlockUpdate = true;
        }
        if (!this.isEmpty()) {
            ListIterator<ItemInPipe> iterator = this.contents.listIterator();
            while (iterator.hasNext()) {
                ItemInPipe item = iterator.next();
                if (this.tickAdded.containsKey(item)) {
                    if (this.tickAdded.get(item) == level.getGameTime()) {
                        continue;
                    }
                    this.tickAdded.remove(item);
                }
                item.move(this.getTargetSpeed(), this.getAcceleration());
                if (item.getAge() > ItemInPipe.DESPAWN_AGE) {
                    iterator.remove();
                    sendBlockUpdate = true;
                    continue;
                }
                if (item.getProgress() >= ItemInPipe.HALFWAY) {
                    if (item.isEjecting()) {
                        iterator.remove();
                        this.eject(level, pos, item);
                        sendBlockUpdate = true;
                    }
                }
                if (item.getProgress() >= ItemInPipe.PIPE_LENGTH) {
                    if (Services.LOADER_SERVICE.handleItemInsertion(this, level, pos, state, item)) {
                        iterator.remove();
                    }
                    sendBlockUpdate = true;
                }
            }
            this.addQueuedItems(level, false); // sets changed
        }
        if (sendBlockUpdate) {
            level.sendBlockUpdated(pos, state, state, 2);
        }
    }

    public void tickClient(Level level, BlockPos pos) {
        if (!this.isEmpty()) {
            ListIterator<ItemInPipe> iterator = this.contents.listIterator();
            while (iterator.hasNext()) {
                ItemInPipe item = iterator.next();
                if (this.tickAdded.containsKey(item)) {
                    if (this.tickAdded.get(item) == level.getGameTime()) {
                        continue;
                    }
                    this.tickAdded.remove(item);
                }
                item.move(this.getTargetSpeed(), this.getAcceleration());
                if (item.getProgress() >= ItemInPipe.PIPE_LENGTH) {
                    BlockPos nextPos = pos.relative(item.getTargetDirection());
                    if (level.getBlockEntity(nextPos) instanceof AbstractPipeEntity nextPipe) {
                        item.resetProgress(item.getTargetDirection().getOpposite());
                        nextPipe.insertPipeItem(level, item);
                        iterator.remove();
                    }
                }
            }
        }
    }

    public void insertPipeItem(Level level, ItemInPipe item) {
        this.queued.add(item);
        this.routeItem(item);
        this.addQueuedItems(level, true);
    }

    public void addQueuedItems(Level level, boolean waitForNextTick) {
        for (ItemInPipe item : this.queued) {
            this.contents.add(item);
            if (waitForNextTick) {
                this.tickAdded.put(item, level.getGameTime());
            }
        }
        this.setChanged();
        this.queued.clear();
    }

    public abstract void routeItem(BlockState state, ItemInPipe item);

    public final void routeItem(ItemInPipe item) {
        routeItem(this.getBlockState(), item);
    }

    protected boolean canJoinLogisticalNetwork() {
        return true;
    }

    protected final boolean isPipeConnected(BlockState state, Direction direction) {
        if (state.getBlock() instanceof AbstractPipeBlock pipeBlock) {
            return pipeBlock.isPipeConnected(state, direction);
        }
        return false;
    }

    protected final int countConnections(BlockState state) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (isPipeConnected(state, direction)) {
                count++;
            }
        }
        return count;
    }

    public void update(ServerLevel level, BlockState state, BlockPos pos, Direction direction, boolean wasConnected) {
        if (!this.isEmpty()) {
            ListIterator<ItemInPipe> iterator = this.contents.listIterator();
            while (iterator.hasNext()) {
                ItemInPipe item = iterator.next();
                if (!wasConnected || (item.getTargetDirection() == direction && item.getProgress() < ItemInPipe.HALFWAY)) {
                    this.routeItem(state, item);
                } else if ((item.getFromDirection() == direction && item.getProgress() < ItemInPipe.HALFWAY) || (item.getTargetDirection() == direction && item.getProgress() >= ItemInPipe.HALFWAY)) {
                    iterator.remove();
                    item.drop(level, pos);
                }
            }
            this.addQueuedItems(level, false);
        }
        if (wasConnected) {
            this.logistics.remove(direction);
        }
        for (Direction otherDirection : Direction.values()) {
            BlockPos nextPos = pos.relative(otherDirection);
            if (this.isPipeConnected(state, otherDirection) && level.getBlockEntity(nextPos) instanceof AbstractPipeEntity nextPipe) {
                this.updateLogistics(level, state, pos, nextPipe, nextPos, otherDirection, new HashSet<>(), true);
            } else {
                this.logistics.remove(otherDirection);
                if (this instanceof LogisticalPipeEntity logisticalPipe) {
                    logisticalPipe.networkChanged(level, pos, false);
                }
            }
        }
        this.setChanged();
        level.sendBlockUpdated(pos, state, state, 2);
    }

    protected void initialiseLogistics(ServerLevel level, BlockState state, BlockPos pos) {
        Set<BlockPos> visited = new HashSet<>();
        for (Direction direction : Direction.values()) {
            BlockPos nextPos = pos.relative(direction);
            if (this.isPipeConnected(state, direction) && level.getBlockEntity(nextPos) instanceof AbstractPipeEntity nextPipe) {
                visited = this.updateLogistics(level, state, pos, nextPipe, nextPos, direction, visited, false);
            } else {
                this.logistics.remove(direction);
            }
        }
        this.setChanged();
    }

    private Set<BlockPos> updateLogistics(ServerLevel level, BlockState state, BlockPos pos, AbstractPipeEntity nextPipe, BlockPos nextPos, Direction nextDirection, Set<BlockPos> visited, boolean triggerNetworkChanges) {

        if (visited.contains(pos)) {
            return visited;
        }
        visited.add(pos);

        if (this instanceof LogisticalPipeEntity && nextPipe.canJoinLogisticalNetwork()) {
            nextPipe.logistics.put(nextDirection.getOpposite(), new Tuple<>(pos, 1));
        } else {
            boolean hasLogisticConnection = false;
            if (this.countConnections(state) < 3) {
                for (Direction direction : this.logistics.keySet()) {
                    if (!direction.equals(nextDirection) && nextPipe.canJoinLogisticalNetwork()) {
                        Tuple<BlockPos, Integer> tuple = this.logistics.get(direction);
                        nextPipe.logistics.put(nextDirection.getOpposite(), new Tuple<>(tuple.a(), tuple.b() + 1));
                        hasLogisticConnection = true;
                    }
                }
            }
            if (!hasLogisticConnection) {
                nextPipe.logistics.remove(nextDirection.getOpposite());
            }
        }

        if (nextPipe instanceof LogisticalPipeEntity && this.canJoinLogisticalNetwork()) {
            this.logistics.put(nextDirection, new Tuple<>(nextPos, 1));
        } else {
            boolean hasLogisticConnection = false;
            if (nextPipe.countConnections(nextPipe.getBlockState()) < 3) {
                for (Direction direction : nextPipe.logistics.keySet()) {
                    if (!direction.equals(nextDirection.getOpposite()) && this.canJoinLogisticalNetwork()) {
                        Tuple<BlockPos, Integer> tuple = nextPipe.logistics.get(direction);
                        this.logistics.put(nextDirection, new Tuple<>(tuple.a(), tuple.b() + 1));
                        hasLogisticConnection = true;
                    }
                }
            }
            if (!hasLogisticConnection) {
                this.logistics.remove(nextDirection);
            }
            for (Direction direction : Direction.values()) {
                if (!direction.equals(nextDirection.getOpposite())) {
                    BlockPos anotherPos = nextPos.relative(direction);
                    if (nextPipe.isPipeConnected(nextPipe.getBlockState(), direction) && level.getBlockEntity(anotherPos) instanceof AbstractPipeEntity anotherPipe) {
                        visited = nextPipe.updateLogistics(level, nextPipe.getBlockState(), nextPos, anotherPipe, anotherPos, direction, visited, triggerNetworkChanges);
                    } else {
                        nextPipe.logistics.remove(direction);
                    }
                }
            }
        }

        if (this instanceof LogisticalPipeEntity logisticalPipe && state.getBlock() instanceof RoutingPipeBlock logisticalBlock) {
            boolean wasLinked = logisticalBlock.isLinked(state, nextDirection);
            boolean isLinked = this.logistics.containsKey(nextDirection);
            if (wasLinked != isLinked && triggerNetworkChanges) {
                logisticalPipe.networkChanged(level, pos, isLinked);
            }
            level.setBlock(pos, logisticalBlock.setLinked(state, nextDirection, isLinked), 3);
        }

        if (nextPipe instanceof LogisticalPipeEntity logisticalPipe && logisticalPipe.getBlockState().getBlock() instanceof RoutingPipeBlock logisticalBlock) {
            boolean wasLinked = logisticalBlock.isLinked(logisticalPipe.getBlockState(), nextDirection.getOpposite());
            boolean isLinked = logisticalPipe.logistics.containsKey(nextDirection.getOpposite());
            if (wasLinked != isLinked && triggerNetworkChanges) {
                logisticalPipe.networkChanged(level, nextPos, isLinked);
            }
            level.setBlock(nextPos, logisticalBlock.setLinked(logisticalPipe.getBlockState(), nextDirection.getOpposite(), isLinked), 3);
        }

        this.setChanged();
        level.sendBlockUpdated(pos, state, state, 2);
        nextPipe.setChanged();
        level.sendBlockUpdated(nextPos, nextPipe.getBlockState(), nextPipe.getBlockState(), 2);
        return visited;

    }

    public void eject(ServerLevel level, BlockPos pos, ItemInPipe item) {
        if (!item.getStack().isEmpty()) {
            Vec3 offset = new Vec3(
                    item.getTargetDirection() == Direction.WEST ? 0.125F : (item.getTargetDirection() == Direction.EAST ? 0.875F : 0.5F),
                    item.getTargetDirection() == Direction.DOWN ? 0.0F : (item.getTargetDirection() == Direction.UP ? 0.75F : 0.375F),
                    item.getTargetDirection() == Direction.NORTH ? 0.125F : (item.getTargetDirection() == Direction.SOUTH ? 0.875F : 0.5F)
            );
            ItemEntity ejectedItem = new ItemEntity(level, pos.getX() + offset.x, pos.getY() + offset.y, pos.getZ() + offset.z, item.getStack());
            float v = (float) item.getSpeed() / ItemInPipe.PIPE_LENGTH;
            ejectedItem.setDeltaMovement(
                    item.getTargetDirection() == Direction.WEST ? -v : (item.getTargetDirection() == Direction.EAST ? v : 0.0F),
                    item.getTargetDirection() == Direction.DOWN ? -v : (item.getTargetDirection() == Direction.UP ? v : 0.0F),
                    item.getTargetDirection() == Direction.NORTH ? -v : (item.getTargetDirection() == Direction.SOUTH ? v : 0.0F)
            );
            ejectedItem.setDefaultPickUpDelay();
            level.addFreshEntity(ejectedItem);
            level.playSound(ejectedItem, pos, ClassicPipes.PIPE_EJECT_SOUND, SoundSource.BLOCKS);
        }
    }

    public void dropItems(ServerLevel serverLevel, BlockPos pos) {
        for (ItemInPipe item : this.contents) {
            item.drop(serverLevel, pos);
        }
    }

    public abstract short getTargetSpeed();

    public abstract short getAcceleration();

    public List<ItemInPipe> getContents() {
        return this.contents;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (this.getLevel() instanceof ServerLevel serverLevel && !stack.isEmpty()) {
            Direction direction = Direction.from3DDataValue(slot);
            ItemInPipe item = new ItemInPipe(stack, direction, direction.getOpposite());
            this.insertPipeItem(serverLevel, item);
            serverLevel.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
        }
        this.setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        this.clearContent();
        this.logistics.clear();
        this.tickAdded.clear();
        super.loadAdditional(valueInput);
        ValueInput.TypedInputList<ItemInPipe> itemsList = valueInput.listOrEmpty("items", ItemInPipe.CODEC);
        itemsList.forEach(contents::add);
        for (Direction direction : Direction.values()) {
            BlockPos pos = valueInput.read(direction.getName() + "_pos", BlockPos.CODEC).orElse(BlockPos.ZERO);
            valueInput.getInt(direction.getName() + "_distance").ifPresent(distance -> this.logistics.put(direction, new Tuple<>(pos, distance)));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        ValueOutput.TypedOutputList<ItemInPipe> itemsList = valueOutput.list("items", ItemInPipe.CODEC);
        for (ItemInPipe item : this.contents) {
            if (!item.getStack().isEmpty()) {
                itemsList.add(item);
            }
        }
        for (Direction direction : this.logistics.keySet()) {
            Tuple<BlockPos, Integer> tuple = this.logistics.get(direction);
            valueOutput.store(direction.getName() + "_pos", BlockPos.CODEC, tuple.a());
            valueOutput.putInt(direction.getName() + "_distance", tuple.b());
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider levelRegistry) {
        ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(this.problemPath(), ClassicPipes.LOGGER);
        CompoundTag tag;
        try {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(scopedCollector, levelRegistry);
            this.saveAdditional(valueOutput);
            tag = valueOutput.buildResult();
        } catch (Throwable error) {
            try {
                scopedCollector.close();
            } catch (Throwable error2) {
                error.addSuppressed(error2);
            }
            throw error;
        }
        scopedCollector.close();
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
