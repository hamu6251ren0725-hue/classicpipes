package jagm.classicpipes.inventory.container;

import net.minecraft.world.Container;

public interface Filter extends Container {

    void setMatchComponents(boolean matchComponents);

    boolean shouldMatchComponents();

}
