package jagm.classicpipes.inventory.menu;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.blockentity.ProviderPipeEntity;
import jagm.classicpipes.inventory.container.Filter;
import jagm.classicpipes.inventory.container.FilterContainer;
import jagm.classicpipes.network.ClientBoundProviderPipePayload;
import net.minecraft.world.entity.player.Inventory;

public class ProviderPipeMenu extends FilterMenu {

    private boolean leaveOne;

    public ProviderPipeMenu(int id, Inventory playerInventory, ClientBoundProviderPipePayload payload) {
        this(id, playerInventory, new FilterContainer(null, 9, payload.matchComponents()), payload.leaveOne());
    }

    public ProviderPipeMenu(int id, Inventory playerInventory, Filter filter, boolean leaveOne) {
        super(ClassicPipes.PROVIDER_PIPE_MENU, id, filter);
        this.leaveOne = leaveOne;
        for (int j = 0; j < 9; j++) {
            this.addSlot(new FilterSlot(filter, j, 8 + j * 18, 18));
        }
        this.addStandardInventorySlots(playerInventory, 8, 84);
    }

    public boolean shouldLeaveOne() {
        return this.leaveOne;
    }

    public void setLeaveOne(boolean leaveOne) {
        this.leaveOne = leaveOne;
        if (this.getFilter().getPipe() instanceof ProviderPipeEntity providerPipe) {
            providerPipe.setLeaveOne(leaveOne);
        }
    }

}
