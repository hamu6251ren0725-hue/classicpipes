package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.inventory.DiamondPipeMenu;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiamondPipeEntity extends RoundRobinPipeEntity implements MenuProvider {

    private static final int FILTER_SIZE = 9;

    private final Map<Direction, List<Item>> FILTER_MAP = new HashMap<>();

    public DiamondPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.DIAMOND_PIPE_ENTITY, pos, state);
        this.initFilters();
    }

    private void initFilters() {
        for (Direction direction : Direction.values()) {
            List<Item> list = new ArrayList<>();
            for (int i = 0; i < FILTER_SIZE; i++) {
                list.add(null);
            }
            FILTER_MAP.put(direction, list);
        }
    }

    @Override
    protected List<Direction> getValidDirections(BlockState state, ItemInPipe item) {
        List<Direction> validDirections = new ArrayList<>();
        Direction direction = MiscUtil.nextDirection(item.getFromDirection());
        for (int i = 0; i < 5; i++) {
            if (this.isPipeConnected(state, direction) && FILTER_MAP.get(direction).contains(item.getStack().getItem())) {
                validDirections.add(direction);
            }
            direction = MiscUtil.nextDirection(direction);
        }
        if (validDirections.isEmpty() && FILTER_MAP.get(item.getFromDirection()).contains(item.getStack().getItem())) {
            validDirections.add(item.getFromDirection());
        }
        if (validDirections.isEmpty()) {
            direction = MiscUtil.nextDirection(item.getFromDirection());
            for (int i = 0; i < 5; i++) {
                if (this.isPipeConnected(state, direction) && FILTER_MAP.get(direction).isEmpty()) {
                    validDirections.add(direction);
                }
                direction = MiscUtil.nextDirection(direction);
            }
        }
        if (validDirections.isEmpty() && FILTER_MAP.get(item.getFromDirection()).isEmpty()) {
            validDirections.add(item.getFromDirection());
        }
        return validDirections;
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        this.initFilters();
        super.loadAdditional(valueInput);
        for (Direction direction : Direction.values()) {
            ValueInput.TypedInputList<ItemStackWithSlot> filterList = valueInput.listOrEmpty("filter_" + direction.name(), ItemStackWithSlot.CODEC);
            for (ItemStackWithSlot slotStack : filterList) {
                FILTER_MAP.get(direction).set(slotStack.slot(), slotStack.stack().getItem());
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        for (Direction direction : Direction.values()) {
            ValueOutput.TypedOutputList<ItemStackWithSlot> filterList = valueOutput.list("filter_" + direction.name(), ItemStackWithSlot.CODEC);
            for (int i = 0; i < FILTER_SIZE; i++) {
                Item item = FILTER_MAP.get(direction).get(i);
                if (item != null) {
                    filterList.add(new ItemStackWithSlot(i, new ItemStack(item)));
                }
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container." + ClassicPipes.MOD_ID + ".diamond_pipe");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new DiamondPipeMenu(id, inventory);
    }
}
