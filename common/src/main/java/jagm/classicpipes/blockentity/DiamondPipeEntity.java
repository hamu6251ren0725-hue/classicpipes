package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.inventory.DiamondPipeMenu;
import jagm.classicpipes.inventory.FilterContainer;
import jagm.classicpipes.util.ItemInPipe;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public class DiamondPipeEntity extends RoundRobinPipeEntity implements MenuProvider {

    private final FilterContainer filter;

    public DiamondPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.DIAMOND_PIPE_ENTITY, pos, state);
        this.filter = new FilterContainer(this);
    }

    @Override
    protected List<Direction> getValidDirections(BlockState state, ItemInPipe item) {
        List<Direction> validDirections = new ArrayList<>();
        Direction direction = MiscUtil.nextDirection(item.getFromDirection());
        for (int i = 0; i < 5; i++) {
            if (this.isPipeConnected(state, direction) && filter.directionContains(item.getStack().getItem(), direction)) {
                validDirections.add(direction);
            }
            direction = MiscUtil.nextDirection(direction);
        }
        if (validDirections.isEmpty() && filter.directionContains(item.getStack().getItem(), direction)) {
            validDirections.add(item.getFromDirection());
        }
        if (validDirections.isEmpty()) {
            direction = MiscUtil.nextDirection(item.getFromDirection());
            for (int i = 0; i < 5; i++) {
                if (this.isPipeConnected(state, direction) && filter.directionEmpty(direction)) {
                    validDirections.add(direction);
                }
                direction = MiscUtil.nextDirection(direction);
            }
        }
        if (validDirections.isEmpty() && filter.directionEmpty(item.getFromDirection())) {
            validDirections.add(item.getFromDirection());
        }
        return validDirections;
    }

    @Override
    protected boolean canJoinLogisticalNetwork() {
        return false;
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        filter.clearContent();
        super.loadAdditional(valueInput);
        ValueInput.TypedInputList<ItemStackWithSlot> filterList = valueInput.listOrEmpty("filter", ItemStackWithSlot.CODEC);
        for (ItemStackWithSlot slotStack : filterList) {
            filter.setItem(slotStack.slot(), slotStack.stack());
        }
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        ValueOutput.TypedOutputList<ItemStackWithSlot> filterList = valueOutput.list("filter", ItemStackWithSlot.CODEC);
        for (int slot = 0; slot < filter.getContainerSize(); slot++) {
            ItemStack stack = filter.getItem(slot);
            if (!stack.isEmpty()) {
                filterList.add(new ItemStackWithSlot(slot, stack));
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container." + ClassicPipes.MOD_ID + ".diamond_pipe");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new DiamondPipeMenu(id, playerInventory, filter);
    }

    public FilterContainer getFilter() {
        return filter;
    }

}
