package jagm.classicpipes.inventory.container;

import jagm.classicpipes.blockentity.PipeEntity;
import net.minecraft.world.Container;

public interface Filter extends Container {

    void setMatchComponents(boolean matchComponents);

    boolean shouldMatchComponents();

    PipeEntity getPipe();

}
