package jagm.classicpipes.inventory.menu;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.inventory.container.DirectionalFilterContainer;
import jagm.classicpipes.inventory.container.Filter;
import jagm.classicpipes.services.Services;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public class DiamondFluidPipeMenu extends AbstractContainerMenu {

    private static final ResourceLocation EMPTY_SLOT = MiscUtil.resourceLocation("container/slot/fluid");

    private final DirectionalFilterContainer filter;

    public DiamondFluidPipeMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new DirectionalFilterContainer(null, false));
    }

    public DiamondFluidPipeMenu(int id, Inventory playerInventory, DirectionalFilterContainer filter) {
        super(ClassicPipes.DIAMOND_FLUID_PIPE_MENU, id);
        this.filter = filter;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                int column = j;
                this.addSlot(new FilterSlot(filter, j + i * 9, 8 + j * 18, 18 + i * 18) {
                    public ResourceLocation getNoItemIcon() {
                        return column == 0 ? EMPTY_SLOT : null;
                    }
                });
            }
        }
        this.addStandardInventorySlots(playerInventory, 8, 154);
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
                    slot.setChanged();
                } else {
                    Fluid fluid = Services.LOADER_SERVICE.getFluidFromStack(this.getCarried());
                    if (fluid != null) {
                        slot.set(new ItemStack(fluid.getBucket()));
                        slot.setChanged();
                    }
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
                Fluid fluid = Services.LOADER_SERVICE.getFluidFromStack(slot.getItem());
                if (fluid != null) {
                    for (int i = 0; i < this.filter.getContainerSize(); i++) {
                        if (!this.slots.get(i).hasItem()) {
                            Slot toSlot = this.slots.get(i);
                            toSlot.set(new ItemStack(fluid.getBucket()));
                            toSlot.setChanged();
                            break;
                        }
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
