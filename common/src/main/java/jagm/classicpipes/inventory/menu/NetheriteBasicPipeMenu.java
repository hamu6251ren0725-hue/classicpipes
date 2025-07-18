package jagm.classicpipes.inventory.menu;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.blockentity.NetheriteBasicPipeEntity;
import jagm.classicpipes.inventory.container.Filter;
import jagm.classicpipes.inventory.container.FilterContainer;
import jagm.classicpipes.network.ClientBoundNetheritePipePayload;
import net.minecraft.world.entity.player.Inventory;

public class NetheriteBasicPipeMenu extends FilterMenu {

    private boolean defaultRoute;

    public NetheriteBasicPipeMenu(int id, Inventory playerInventory, ClientBoundNetheritePipePayload payload) {
        this(id, playerInventory, new FilterContainer(null, 9, payload.matchComponents()), payload.defaultRoute());
    }

    public NetheriteBasicPipeMenu(int id, Inventory playerInventory, Filter filter, boolean defaultRoute) {
        super(ClassicPipes.NETHERITE_BASIC_PIPE_MENU, id, filter);
        this.defaultRoute = defaultRoute;
        for (int j = 0; j < 9; j++) {
            this.addSlot(new FilterSlot(filter, j, 8 + j * 18, 18));
        }
        this.addStandardInventorySlots(playerInventory, 8, 84);
    }

    public boolean isDefaultRoute() {
        return this.defaultRoute;
    }

    public void setDefaultRoute(boolean defaultRoute) {
        this.defaultRoute = defaultRoute;
        if (this.getFilter().getPipe() instanceof NetheriteBasicPipeEntity netheritePipe) {
            netheritePipe.setDefaultRoute(defaultRoute);
        }
    }

}
