package jagm.classicpipes.inventory.menu;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.inventory.container.RequestMenuContainer;
import jagm.classicpipes.network.ClientBoundItemListPayload;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public class RequestMenu extends AbstractContainerMenu {

    Container container;

    public RequestMenu(int id, ClientBoundItemListPayload payload) {
        super(ClassicPipes.REQUEST_MENU, id);
        this.container = new RequestMenuContainer(payload.toDisplay());
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new FilterSlot(this.container, i * 8 + j, 8 + j * 18, 18 + i * 18));
            }
        }
    }

    @Override
    public void clicked(int index, int button, ClickType clickType, Player player) {
        if (index >= 0 && index < this.container.getContainerSize()) {
            if (clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) {
                // TODO open request subscreen
            } else if (clickType == ClickType.SWAP) {
                // TODO quick-request items
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

}
