package jagm.classicpipes.inventory.container;

import jagm.classicpipes.blockentity.ItemPipeEntity;

public class SingleItemFilterContainer extends FilterContainer {

    public SingleItemFilterContainer(ItemPipeEntity pipe, int size, boolean matchComponents) {
        super(pipe, size, matchComponents);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

}
