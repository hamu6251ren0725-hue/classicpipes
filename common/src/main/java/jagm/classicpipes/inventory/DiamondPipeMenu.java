package jagm.classicpipes.inventory;

import jagm.classicpipes.ClassicPipes;
import net.minecraft.world.entity.player.Inventory;

public class DiamondPipeMenu extends FilterMenu {

    public DiamondPipeMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new DirectionalFilterContainer());
    }

    public DiamondPipeMenu(int id, Inventory playerInventory, Filter filter) {
        super(ClassicPipes.DIAMOND_PIPE_MENU, id, filter);
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new FilterSlot(filter, j + i * 9, 8 + j * 18, 18 + i * 18));
            }
        }
        this.addStandardInventorySlots(playerInventory, 8, 154);
    }

}
