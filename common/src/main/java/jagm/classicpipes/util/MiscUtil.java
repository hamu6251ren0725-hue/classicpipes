package jagm.classicpipes.util;

import jagm.classicpipes.ClassicPipes;
import net.minecraft.resources.ResourceLocation;

public class MiscUtil {

    public static ResourceLocation resourceLocation(String name) {
        return ResourceLocation.fromNamespaceAndPath(ClassicPipes.MOD_ID, name);
    }

}
