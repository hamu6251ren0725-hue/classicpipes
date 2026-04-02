package jagm.classicpipes.inventory.menu;

import jagm.classicpipes.inventory.container.Filter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
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
    public void clicked(int index, int button, ContainerInput containerInput, Player player) {
        if (index >= this.filter.getContainerSize() || index < 0 || containerInput.equals(ContainerInput.CLONE)) {
            super.clicked(index, button, containerInput, player);
        } else {
            Slot slot = this.slots.get(index);
            if ((containerInput == ContainerInput.PICKUP || containerInput == ContainerInput.QUICK_MOVE) && (button == 0 || button == 1)) {
                if (this.getCarried().isEmpty() || containerInput == ContainerInput.QUICK_MOVE) {
                    slot.remove(1);
                } else {
                    slot.set(this.getCarried().copyWithCount(1));
                }
                slot.setChanged();
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
                        Slot toSlot = this.slots.get(i);
                        toSlot.set(slot.getItem().copyWithCount(1));
                        toSlot.setChanged();
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
