package jagm.classicpipes.inventory.container;

import jagm.classicpipes.blockentity.AbstractPipeEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FilterContainer implements Filter {

    private final NonNullList<ItemStack> filter;
    private final AbstractPipeEntity pipe;
    private final int size;
    private boolean matchComponents;

    public FilterContainer(AbstractPipeEntity pipe, int size) {
        this.pipe = pipe;
        this.filter = NonNullList.create();
        this.size = size;
        this.matchComponents = false;
        this.clearContent();
    }

    public FilterContainer() {
        this(null, 9);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getContainerSize() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.filter.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.filter.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        this.filter.set(slot, ItemStack.EMPTY);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return removeItem(slot, 1);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.filter.set(slot, stack);
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
        this.filter.clear();
        this.filter.addAll(NonNullList.withSize(this.size, ItemStack.EMPTY));
    }

    @Override
    public void setMatchComponents(boolean matchComponents) {
        this.matchComponents = matchComponents;
    }

    @Override
    public boolean shouldMatchComponents() {
        return this.matchComponents;
    }

}
