package jagm.classicpipes.inventory.menu;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.inventory.container.Filter;
import jagm.classicpipes.inventory.container.FilterContainer;
import net.minecraft.world.entity.player.Inventory;

public class ProviderPipeMenu extends FilterMenu {

    public ProviderPipeMenu(int id, Inventory playerInventory, boolean matchComponents) {
        this(id, playerInventory, new FilterContainer(null, 9, matchComponents));
    }

    public ProviderPipeMenu(int id, Inventory playerInventory, Filter filter) {
        super(ClassicPipes.PROVIDER_PIPE_MENU, id, filter);
        for (int j = 0; j < 9; j++) {
            this.addSlot(new FilterSlot(filter, j, 8 + j * 18, 18));
        }
        this.addStandardInventorySlots(playerInventory, 8, 84);
    }

}
