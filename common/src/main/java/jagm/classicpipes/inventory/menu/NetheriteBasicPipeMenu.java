package jagm.classicpipes.inventory.menu;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.inventory.container.Filter;
import jagm.classicpipes.inventory.container.FilterContainer;
import jagm.classicpipes.network.MatchComponentsPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class NetheriteBasicPipeMenu extends FilterMenu {

    public NetheriteBasicPipeMenu(int id, Inventory playerInventory, boolean matchComponents) {
        this(id, playerInventory, new FilterContainer(null, 9, matchComponents));
    }

    public NetheriteBasicPipeMenu(int id, Inventory playerInventory, Filter filter) {
        super(ClassicPipes.NETHERITE_BASIC_PIPE_MENU, id, filter);
        for (int j = 0; j < 9; j++) {
            this.addSlot(new FilterSlot(filter, j, 8 + j * 18, 18));
        }
        this.addStandardInventorySlots(playerInventory, 8, 84);
    }

}
