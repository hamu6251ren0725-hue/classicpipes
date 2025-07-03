package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.block.AbstractPipeBlock;
import jagm.classicpipes.services.Services;
import jagm.classicpipes.util.ItemInPipe;
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
import net.minecraft.util.Tuple;
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

    public AbstractPipeEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.contents = new ArrayList<>();
        this.queued = new ArrayList<>();
        this.logistics = new HashMap<>();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (blockEntity instanceof AbstractPipeEntity pipe) {
            if (level instanceof ServerLevel serverLevel) {
                pipe.tickServer(serverLevel, pos, state);
            } else {
                pipe.tickClient(level, pos, state);
            }
        }
    }

    public void tickServer(ServerLevel level, BlockPos pos, BlockState state) {
        if (!this.logisticsInitialised) {
            this.updateLogistics(level, state, pos);
            this.logisticsInitialised = true;
        }
        if (!this.isEmpty()) {
            ListIterator<ItemInPipe> iterator = this.contents.listIterator();
            while (iterator.hasNext()) {
                ItemInPipe item = iterator.next();
                item.move(level, this.getTargetSpeed(), this.getAcceleration());
                if (item.getProgress() >= ItemInPipe.HALFWAY) {
                    if (item.isEjecting()) {
                        this.routeItem(item);
                        if (item.isEjecting()) {
                            iterator.remove();
                            this.eject(level, pos, item);
                        }
                        level.sendBlockUpdated(pos, state, state, 2);
                    }
                }
                if (item.getProgress() >= ItemInPipe.PIPE_LENGTH) {
                    if (Services.BLOCK_ENTITY_HELPER.handleItemInsertion(level, pos, item)) {
                        iterator.remove();
                    }
                    level.sendBlockUpdated(pos, state, state, 2);
                }
            }
            this.setChanged();
            this.addQueuedItems();
        }
    }

    public void tickClient(Level level, BlockPos pos, BlockState state) {
        if (!this.isEmpty()) {
            for (ItemInPipe item : this.contents) {
                item.move(level, this.getTargetSpeed(), this.getAcceleration());
            }
            this.setChanged();
        }
    }

    public void insertPipeItem(Level level, ItemInPipe item) {
        item.setCreatedThisTick(true);
        item.setEvenTick(level.getGameTime() % 2 == 0);
        this.contents.add(item);
        this.setChanged();
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
        return state.getValue(AbstractPipeBlock.PROPERTY_BY_DIRECTION.get(direction));
    }

    public void update(ServerLevel level, BlockState state, BlockPos pos, Direction direction, boolean wasConnected) {
        if (!this.isEmpty()) {
            ListIterator<ItemInPipe> iterator = this.contents.listIterator();
            while (iterator.hasNext()) {
                ItemInPipe item = iterator.next();
                if (wasConnected) {
                    if ((item.getFromDirection() == direction && item.getProgress() < ItemInPipe.HALFWAY) || (item.getTargetDirection() == direction && item.getProgress() >= ItemInPipe.HALFWAY)) {
                        iterator.remove();
                        item.drop(level, pos);
                    }
                }
            }
            this.setChanged();
            level.sendBlockUpdated(pos, state, state, 2);
        }
        this.updateLogistics(level, state, pos, direction, new HashSet<>());
    }

    private void updateLogistics(ServerLevel level, BlockState state, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (state.getValue(AbstractPipeBlock.PROPERTY_BY_DIRECTION.get(direction))) {
                this.updateLogistics(level, state, pos, direction, new HashSet<>());
            }
        }
    }

    private void updateLogistics(ServerLevel level, BlockState state, BlockPos pos, Direction direction, Set<BlockPos> visited) {
        if (visited.contains(pos)) {
            return;
        }
        visited.add(pos);
        BlockPos neighbourPos = pos.relative(direction);
        if (level.getBlockEntity(neighbourPos) instanceof AbstractPipeEntity neighbourPipe) {
            if (neighbourPipe instanceof LogisticalPipeEntity) {
                this.logistics.put(direction, new Tuple<>(neighbourPos, 1));
            } else if (neighbourPipe.logistics.size() < (neighbourPipe.logistics.containsKey(direction.getOpposite()) ? 3 : 2) && neighbourPipe.logistics.size() > (neighbourPipe.logistics.containsKey(direction.getOpposite()) ? 1 : 0)) {
                for (Direction neighbourToLogistical : neighbourPipe.logistics.keySet()) {
                    if (!neighbourToLogistical.equals(direction.getOpposite())){
                        Tuple<BlockPos, Integer> tuple = neighbourPipe.logistics.get(neighbourToLogistical);
                        this.logistics.put(direction, new Tuple<>(tuple.getA(), tuple.getB() + 1));
                    }
                }
            } else {
                this.logistics.remove(direction);
            }
            for (Direction otherDirection : this.logistics.keySet()) {
                if (!otherDirection.equals(direction)) {
                    if (this instanceof LogisticalPipeEntity) {
                        neighbourPipe.logistics.put(direction.getOpposite(), new Tuple<>(pos, 1));
                    } else if (this.logistics.size() < (this.logistics.containsKey(direction) ? 3 : 2) && this.logistics.size() > (this.logistics.containsKey(direction) ? 1 : 0)) {
                        Tuple<BlockPos, Integer> tuple = this.logistics.get(otherDirection);
                        neighbourPipe.logistics.put(direction.getOpposite(), new Tuple<>(tuple.getA(), tuple.getB() + 1));
                    } else {
                        neighbourPipe.logistics.remove(direction.getOpposite());
                    }
                    neighbourPipe.setChanged();
                    level.sendBlockUpdated(neighbourPos, neighbourPipe.getBlockState(), neighbourPipe.getBlockState(), 2);
                    if (level.getBlockEntity(pos.relative(otherDirection)) instanceof AbstractPipeEntity otherNeighbour) {
                        otherNeighbour.updateLogistics(level, otherNeighbour.getBlockState(), pos.relative(otherDirection), otherDirection.getOpposite(), visited);
                    }
                }
            }
            neighbourPipe.updateLogistics(level, neighbourPipe.getBlockState(), neighbourPos, direction.getOpposite(), visited);
        } else {
            this.logistics.remove(direction);
            for (Direction otherDirection : this.logistics.keySet()) {
                if (level.getBlockEntity(pos.relative(otherDirection)) instanceof AbstractPipeEntity otherNeighbour) {
                    otherNeighbour.updateLogistics(level, otherNeighbour.getBlockState(), pos.relative(otherDirection), otherDirection.getOpposite(), visited);
                }
            }
        }
        this.setChanged();
        level.sendBlockUpdated(pos, state, state, 2);
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

    public abstract int getTargetSpeed();

    public abstract int getAcceleration();

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
        super.loadAdditional(valueInput);
        ValueInput.TypedInputList<ItemInPipe> itemsList = valueInput.listOrEmpty("items", ItemInPipe.CODEC);
        itemsList.forEach(contents::add);
        for (Direction direction : Direction.values()) {
            BlockPos pos = valueInput.read(direction.getName() + "_pos", BlockPos.CODEC).orElse(BlockPos.ZERO);
            int distance = valueInput.getIntOr(direction.getName() + "_distance", -1);
            if (distance >= 0) {
                this.logistics.put(direction, new Tuple<>(pos, distance));
            }
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
            valueOutput.store(direction.getName() + "_pos", BlockPos.CODEC, tuple.getA());
            valueOutput.putInt(direction.getName() + "_distance", tuple.getB());
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
