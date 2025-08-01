package jagm.classicpipes.util;

import net.minecraft.world.item.ItemStack;

public record MissingItem(ItemStack stack, MissingItem... ingredients) {}
