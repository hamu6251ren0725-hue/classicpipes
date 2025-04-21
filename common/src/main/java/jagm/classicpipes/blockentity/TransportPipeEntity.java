package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.block.TransportPipeBlock;
import jagm.classicpipes.util.ItemInPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class TransportPipeEntity extends BlockEntity implements WorldlyContainer {

    protected final List<ItemInPipe> contents;

    public TransportPipeEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.contents = new ArrayList<>();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if(blockEntity instanceof TransportPipeEntity pipeEntity && level instanceof ServerLevel serverLevel){
            List<ItemInPipe> toRemove = new ArrayList<>();
            List<ItemInPipe> toAdd = new ArrayList<>();
            for(ItemInPipe pipeItem : pipeEntity.contents){
                if(pipeItem.getStack().isEmpty() || pipeItem.isDestroyed()){
                    toRemove.add(pipeItem);
                    continue;
                }
                pipeItem.move();
                boolean fromDisconnected = !pipeEntity.isPipeConnected(pipeItem.getFromDirection());
                boolean targetDisconnected = !pipeEntity.isPipeConnected(pipeItem.getTargetDirection());
                if(targetDisconnected && !pipeItem.isEjecting() && pipeItem.getProgress() < ItemInPipe.HALFWAY){
                    ClassicPipes.LOGGER.info("Pipe disconnected! Rerouting {}x {}.", pipeItem.getStack().getCount(), pipeItem.getStack().getDisplayName().getString());
                    toAdd.addAll(pipeEntity.routeItem(pipeItem));
                }
                if(targetDisconnected && !pipeItem.isEjecting() && pipeItem.getProgress() >= ItemInPipe.HALFWAY){
                    ClassicPipes.LOGGER.info("Pipe disconnected! Setting {}x {} to eject.", pipeItem.getStack().getCount(), pipeItem.getStack().getDisplayName().getString());
                    pipeItem.eject();
                }
                boolean timeToEject = pipeItem.isEjecting() && pipeItem.getProgress() >= ItemInPipe.HALFWAY;
                if(timeToEject || (fromDisconnected && pipeItem.getProgress() < ItemInPipe.HALFWAY)){
                    ClassicPipes.LOGGER.info("Ejecting {}x {}!", pipeItem.getStack().getCount(), pipeItem.getStack().getDisplayName().getString());
                    pipeItem.drop(serverLevel, pos);
                    pipeItem.destroy();
                }
                else if (pipeItem.getProgress() >= ItemInPipe.PIPE_LENGTH){
                    Container container = HopperBlockEntity.getContainerAt(serverLevel, pos.relative(pipeItem.getTargetDirection()));
                    if (container == null) {
                        ClassicPipes.LOGGER.info("Bouncing {}x {}.", pipeItem.getStack().getCount(), pipeItem.getStack().getDisplayName().getString());
                        pipeItem.resetProgress();
                        pipeItem.setFromDirection(pipeItem.getTargetDirection());
                        toAdd.addAll(pipeEntity.routeItem(pipeItem));
                    }
                    else if(container instanceof TransportPipeEntity neighborPipe){
                        ClassicPipes.LOGGER.info("Passing {}x {} to next pipe.", pipeItem.getStack().getCount(), pipeItem.getStack().getDisplayName().getString());
                        pipeItem.resetProgress();
                        pipeItem.setFromDirection(pipeItem.getTargetDirection().getOpposite());
                        neighborPipe.contents.addAll(neighborPipe.routeItem(pipeItem));
                        neighborPipe.contents.add(pipeItem);
                        toRemove.add(pipeItem);
                    } else {
                        ClassicPipes.LOGGER.info("Attempting to insert {}x {} into container.", pipeItem.getStack().getCount(), pipeItem.getStack().getDisplayName().getString());
                        ItemStack stack = HopperBlockEntity.addItem(pipeEntity, container, pipeItem.getStack(), pipeItem.getTargetDirection().getOpposite());
                        if (stack.isEmpty()) {
                            ClassicPipes.LOGGER.info("Successfully inserted {}x {} into container.", pipeItem.getStack().getCount(), pipeItem.getStack().getDisplayName().getString());
                            pipeItem.destroy();
                        } else {
                            pipeItem.setStack(stack);
                            ClassicPipes.LOGGER.info("Leftover {}x {}.", pipeItem.getStack().getCount(), pipeItem.getStack().getDisplayName().getString());
                            pipeItem.resetProgress();
                            pipeItem.setFromDirection(pipeItem.getTargetDirection());
                            toAdd.addAll(pipeEntity.routeItem(pipeItem));
                        }
                    }
                }
            }
            pipeEntity.contents.removeAll(toRemove);
            pipeEntity.contents.addAll(toAdd);
        }
    }

    public List<ItemInPipe> routeItem(ItemInPipe pipeItem){
        RandomSource random = this.level.getRandom();
        List<ItemInPipe> outputs = new ArrayList<>();
        for(Direction direction : Direction.values()){
            if(isPipeConnected(direction) && !direction.equals(pipeItem.getFromDirection())){
                outputs.add(new ItemInPipe(pipeItem.getStack().copyWithCount(0), pipeItem.getFromDirection(), direction));
            }
        }
        if(outputs.isEmpty()){
            ClassicPipes.LOGGER.info("Setting {}x {} to eject.", pipeItem.getStack().getCount(), pipeItem.getStack().getDisplayName().getString());
            pipeItem.setTargetDirection(pipeItem.getFromDirection().getOpposite());
            pipeItem.eject();
        }else{
            for(int i = 0; i < pipeItem.getStack().getCount(); i++){
                outputs.get(random.nextInt(outputs.size())).getStack().grow(1);
            }
            pipeItem.destroy();
        }
        return outputs;
    }

    protected boolean isPipeConnected(Direction direction){
        return this.getBlockState().getValue(TransportPipeBlock.PROPERTY_BY_DIRECTION.get(direction));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider levelRegistry){
        super.loadAdditional(tag, levelRegistry);
        ListTag pipeItemsList = tag.getListOrEmpty("items");
        for(int i = 0; i < pipeItemsList.size(); i++){
            CompoundTag pipeItemTag = pipeItemsList.getCompoundOrEmpty(i);
            ItemInPipe pipeItem = ItemInPipe.parse(pipeItemTag, levelRegistry);
            if(pipeItem != null){
                if(!pipeItem.getStack().isEmpty()){
                    contents.add(pipeItem);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider levelRegistry){
        super.saveAdditional(tag, levelRegistry);
        ListTag pipeItemsList = new ListTag();
        for(ItemInPipe pipeItem : this.contents){
            if(!pipeItem.getStack().isEmpty()) {
                pipeItemsList.add(pipeItem.save(levelRegistry));
            }
        }
        tag.put("items", pipeItemsList);
    }

    @Override
    public int[] getSlotsForFace(Direction direction){
        return new int[]{direction.get3DDataValue()};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction){
        if(direction != null){
            return slot == direction.get3DDataValue() && this.canPlaceItem(slot, stack);
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction){
        return false;
    }

    @Override
    public int getContainerSize(){
        return 6;
    }

    @Override
    public boolean isEmpty(){
        return this.contents.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot){
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount){
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot){
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack){
        ClassicPipes.LOGGER.info("Adding {}x {} to pipe.", stack.getCount(), stack.getDisplayName().getString());
        Direction direction = Direction.from3DDataValue(slot);
        ItemInPipe pipeItem = new ItemInPipe(stack, direction, direction.getOpposite());
        this.contents.addAll(this.routeItem(pipeItem));
        this.contents.add(pipeItem);
        this.setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack){
        return isPipeConnected(Direction.from3DDataValue(slot));
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return false;
    }

    @Override
    public Iterator<ItemStack> iterator(){
        return new TransportPipeIterator(this.contents);
    }

    @Override
    public void clearContent(){
        this.contents.clear();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    public static class TransportPipeIterator implements Iterator<ItemStack> {

        private final List<ItemInPipe> contents;
        private int index;

        public TransportPipeIterator(List<ItemInPipe> contents){
            this.contents = contents;
        }

        @Override
        public boolean hasNext() {
            return this.index < contents.size();
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
