package jagm.classicpipes.inventory.container;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RequestMenuContainer implements Container {

    NonNullList<ItemStack> contents;

    public RequestMenuContainer(List<ItemStack> contents) {
        this.contents = NonNullList.withSize(72, ItemStack.EMPTY);
        for (int i = 0; i < contents.size(); i++) {
            this.contents.set(i, contents.get(i));
        }
    }

    @Override
    public int getContainerSize() {
        return 72;
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
        this.contents = NonNullList.withSize(72, ItemStack.EMPTY);
    }

}
