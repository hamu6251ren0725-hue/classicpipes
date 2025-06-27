package jagm.classicpipes.inventory;

import jagm.classicpipes.ClassicPipes;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DiamondPipeMenu extends AbstractContainerMenu {

    private final Container filter;

    public DiamondPipeMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new ItemFilterContainer());
    }

    public DiamondPipeMenu(int id, Inventory playerInventory, Container filter) {
        super(ClassicPipes.DIAMOND_PIPE_MENU, id);
        this.filter = filter;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < ItemFilterContainer.FILTER_SIZE; j++) {
                this.addSlot(new Slot(filter, j + i * ItemFilterContainer.FILTER_SIZE, 8 + j * 18, 18 + i * 18));
            }
        }
        this.addStandardInventorySlots(playerInventory, 8, 139);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            if (index < 6 * ItemFilterContainer.FILTER_SIZE) {
                this.moveItemStackTo(stack, 6 * ItemFilterContainer.FILTER_SIZE, this.slots.size(), true);
            } else {
                this.moveItemStackTo(stack, 0, 6 * ItemFilterContainer.FILTER_SIZE, false);
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.filter.stillValid(player);
    }

}
