package jagm.classicpipes.inventory;

import net.minecraft.world.Container;

public interface Filter extends Container {

    void setMatchComponents(boolean matchComponents);

    boolean shouldMatchComponents();

}
