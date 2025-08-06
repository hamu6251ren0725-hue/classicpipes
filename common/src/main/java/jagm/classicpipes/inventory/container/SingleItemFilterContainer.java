package jagm.classicpipes.inventory.container;

import jagm.classicpipes.blockentity.AbstractPipeEntity;

public class SingleItemFilterContainer extends FilterContainer {

    public SingleItemFilterContainer(AbstractPipeEntity pipe, int size, boolean matchComponents) {
        super(pipe, size, matchComponents);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

}
