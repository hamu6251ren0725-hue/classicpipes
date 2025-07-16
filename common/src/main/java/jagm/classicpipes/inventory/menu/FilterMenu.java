package jagm.classicpipes.inventory.menu;

import jagm.classicpipes.inventory.container.Filter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class FilterMenu extends AbstractContainerMenu {

    private final Filter filter;

    protected FilterMenu(MenuType<?> menuType, int id, Filter filter) {
        super(menuType, id);
        this.filter = filter;
    }

    @Override
    public void clicked(int index, int button, ClickType clickType, Player player) {
        if (index >= this.filter.getContainerSize() || index < 0 || clickType.equals(ClickType.CLONE)) {
            super.clicked(index, button, clickType, player);
        } else {
            Slot slot = this.slots.get(index);
            if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (button == 0 || button == 1)) {
                if (this.getCarried().isEmpty() || clickType == ClickType.QUICK_MOVE) {
                    slot.remove(1);
                } else {
                    slot.set(this.getCarried().copyWithCount(1));
                }
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            if (index < this.filter.getContainerSize()) {
                slot.remove(1);
                slot.setChanged();
            } else {
                for (int i = 0; i < this.filter.getContainerSize(); i++) {
                    if (!this.slots.get(i).hasItem()) {
                        this.slots.get(i).set(slot.getItem().copyWithCount(1));
                        break;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return slot.container != this.filter;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.filter.stillValid(player);
    }

    public Filter getFilter() {
        return this.filter;
    }

}
