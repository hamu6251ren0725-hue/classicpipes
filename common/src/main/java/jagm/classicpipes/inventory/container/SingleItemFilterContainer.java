package jagm.classicpipes.inventory.container;

import jagm.classicpipes.blockentity.PipeEntity;

public class SingleItemFilterContainer extends FilterContainer {

    public SingleItemFilterContainer(PipeEntity pipe, int size, boolean matchComponents) {
        super(pipe, size, matchComponents);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

}
