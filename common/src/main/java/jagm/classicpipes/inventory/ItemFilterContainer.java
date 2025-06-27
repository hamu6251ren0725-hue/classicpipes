package jagm.classicpipes.inventory;

import jagm.classicpipes.blockentity.AbstractPipeEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ItemFilterContainer implements Container {

    public static final int FILTER_SIZE = 9;

    private final Map<Direction, NonNullList<ItemStack>> filterMap;
    private final AbstractPipeEntity pipe;

    public ItemFilterContainer(AbstractPipeEntity pipe) {
        this.pipe = pipe;
        this.filterMap = new HashMap<>();
        this.clearContent();
    }

    public ItemFilterContainer() {
        this(null);
    }

    public boolean directionContains(Item item, Direction direction) {
        for (ItemStack stack : filterMap.get(direction)) {
            if (stack.is(item)) {
                return true;
            }
        }
        return false;
    }

    public boolean directionEmpty(Direction direction) {
        return filterMap.get(direction).stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getContainerSize() {
        return FILTER_SIZE * filterMap.size();
    }

    @Override
    public boolean isEmpty() {
        for (Direction direction : Direction.values()) {
            if (!this.directionEmpty(direction)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        int row = slot / FILTER_SIZE;
        int col = slot % FILTER_SIZE;
        return filterMap.get(Direction.from3DDataValue(row)).get(col);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        int row = slot / FILTER_SIZE;
        int col = slot % FILTER_SIZE;
        return ContainerHelper.takeItem(filterMap.get(Direction.from3DDataValue(row)), col);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        int row = slot / FILTER_SIZE;
        int col = slot % FILTER_SIZE;
        return ContainerHelper.takeItem(filterMap.get(Direction.from3DDataValue(row)), col);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        int row = slot / FILTER_SIZE;
        int col = slot % FILTER_SIZE;
        filterMap.get(Direction.from3DDataValue(row)).set(col, stack);
    }

    @Override
    public void setChanged() {
        if (pipe != null) {
            pipe.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (pipe != null) {
            return Container.stillValidBlockEntity(pipe, player);
        } else {
            return true;
        }
    }

    @Override
    public void clearContent() {
        for (Direction direction : Direction.values()) {
            filterMap.put(direction, NonNullList.withSize(FILTER_SIZE, ItemStack.EMPTY));
        }
    }

}
