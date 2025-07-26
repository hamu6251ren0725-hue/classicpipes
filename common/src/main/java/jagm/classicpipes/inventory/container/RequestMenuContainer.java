package jagm.classicpipes.inventory.container;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RequestMenuContainer implements Container {

    private static final int DISPLAY_SIZE = 8 * 9;

    private NonNullList<ItemStack> contents;

    public RequestMenuContainer() {
        this.contents = NonNullList.withSize(DISPLAY_SIZE, ItemStack.EMPTY);
    }

    @Override
    public int getContainerSize() {
        return DISPLAY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return this.contents.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.contents.get(slot);
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
        this.contents.set(slot, stack);
    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.contents = NonNullList.withSize(DISPLAY_SIZE, ItemStack.EMPTY);
    }

}
