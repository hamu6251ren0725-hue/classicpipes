package jagm.classicpipes.inventory;

import jagm.classicpipes.ClassicPipes;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DiamondPipeMenu extends AbstractContainerMenu {

    private final Container filter;

    public DiamondPipeMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new FilterContainer());
    }

    public DiamondPipeMenu(int id, Inventory playerInventory, Container filter) {
        super(ClassicPipes.DIAMOND_PIPE_MENU, id);
        this.filter = filter;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new FilterSlot(filter, j + i * 9, 8 + j * 18, 18 + i * 18));
            }
        }
        this.addStandardInventorySlots(playerInventory, 8, 139);
    }

    @Override
    public void clicked(int index, int button, ClickType clickType, Player player) {
        if (index >= 54 || index < 0 || clickType.equals(ClickType.CLONE)) {
            super.clicked(index, button, clickType, player);
        } else {
            Slot slot = this.slots.get(index);
            if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (button == 0 || button == 1)) {
                if (this.getCarried().isEmpty()) {
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
            if (index < 54) {
                slot.remove(1);
                slot.setChanged();
            } else {
                for (int i = 0; i < 54; i++) {
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

}
