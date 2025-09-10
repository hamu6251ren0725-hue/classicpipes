package jagm.classicpipes.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class LabelItem extends Item {

    public LabelItem(Properties properties) {
        super(properties);
    }

    public abstract boolean itemMatches(ItemStack tagStack, ItemStack compareStack);

}
