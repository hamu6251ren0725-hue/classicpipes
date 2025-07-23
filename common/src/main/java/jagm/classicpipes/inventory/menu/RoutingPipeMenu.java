package jagm.classicpipes.inventory.menu;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.blockentity.RoutingPipeEntity;
import jagm.classicpipes.inventory.container.Filter;
import jagm.classicpipes.inventory.container.FilterContainer;
import jagm.classicpipes.network.ClientBoundRoutingPipePayload;
import net.minecraft.world.entity.player.Inventory;

public class RoutingPipeMenu extends FilterMenu {

    private boolean defaultRoute;

    public RoutingPipeMenu(int id, Inventory playerInventory, ClientBoundRoutingPipePayload payload) {
        this(id, playerInventory, new FilterContainer(null, 9, payload.matchComponents()), payload.defaultRoute());
    }

    public RoutingPipeMenu(int id, Inventory playerInventory, Filter filter, boolean defaultRoute) {
        super(ClassicPipes.ROUTING_PIPE_MENU, id, filter);
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
        if (this.getFilter().getPipe() instanceof RoutingPipeEntity routingPipe) {
            routingPipe.setDefaultRoute(defaultRoute);
        }
    }

}
