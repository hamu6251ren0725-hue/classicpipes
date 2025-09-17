package jagm.classicpipes.client.screen;

import jagm.classicpipes.inventory.menu.FilterMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class FilterScreen<T extends FilterMenu> extends AbstractContainerScreen<T> {

    public FilterScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    public int filterSlots() {
        return this.getMenu().getFilter().getContainerSize();
    }

    public int filterScreenLeft() {
        return this.leftPos;
    }

    public int filterScreenTop() {
        return this.topPos;
    }

}
