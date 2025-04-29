package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.block.AbstractPipeBlock;
import jagm.classicpipes.block.CopperPipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CopperPipeEntity extends RoundRobinPipeEntity {

    private static final int[][] CACHED_SLOTS = new int[54][];
    private static final byte DEFAULT_COOLDOWN = 8;

    private byte cooldown;

    public CopperPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.COPPER_PIPE_ENTITY, pos, state);
        this.cooldown = DEFAULT_COOLDOWN;
    }

    @Override
    public void tickServer(ServerLevel level, BlockPos pos, BlockState state) {
        super.tickServer(level, pos, state);
        if (state.getValue(CopperPipeBlock.ENABLED)) {
            if (this.cooldown-- == 0) {
                Direction direction = state.getValue(CopperPipeBlock.FACING);
                Container container = AbstractPipeBlock.getBlockContainer(level, pos.relative(direction));
                if (container != null && !(container instanceof AbstractPipeEntity)) {
                    int[] slots = getSlots(container, direction.getOpposite());
                    for (int slot = slots.length - 1; slot >= 0; slot--) {
                        ItemStack stack = container.getItem(slot);
                        if (!stack.isEmpty() && canTakeItemFromContainer(this, container, stack, slot, direction.getOpposite())) {
                            int count = stack.getCount();
                            if (HopperBlockEntity.addItem(container, this, container.removeItem(slot, 1), direction).isEmpty()) {
                                container.setChanged();
                                break;
                            } else {
                                stack.setCount(count);
                                if (count == 1) {
                                    container.setItem(slot, stack);
                                }
                            }
                        }
                    }
                }
                this.cooldown = DEFAULT_COOLDOWN;
            }
        }
    }

    private static int[] getSlots(Container container, Direction direction) {
        if (container instanceof WorldlyContainer worldlyContainer) {
            return worldlyContainer.getSlotsForFace(direction);
        } else {
            int i = container.getContainerSize();
            if (i < CACHED_SLOTS.length) {
                int[] aint = CACHED_SLOTS[i];
                if (aint != null) {
                    return aint;
                } else {
                    int[] aint1 = createFlatSlots(i);
                    CACHED_SLOTS[i] = aint1;
                    return aint1;
                }
            } else {
                return createFlatSlots(i);
            }
        }
    }

    private static int[] createFlatSlots(int size) {
        int[] aint = new int[size];
        for (int i = 0; i < aint.length; aint[i] = i++);
        return aint;
    }

    private static boolean canTakeItemFromContainer(Container source, Container destination, ItemStack stack, int slot, Direction direction) {
        if (!destination.canTakeItem(source, slot, stack)) {
            return false;
        } else {
            if (destination instanceof WorldlyContainer worldlyContainer) {
                return worldlyContainer.canTakeItemThroughFace(slot, stack, direction);
            }
            return true;
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        super.loadAdditional(tag, levelRegistry);
        this.cooldown = tag.getByteOr("cooldown", DEFAULT_COOLDOWN);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        super.saveAdditional(tag, levelRegistry);
        tag.putByte("cooldown", this.cooldown);
    }

}
