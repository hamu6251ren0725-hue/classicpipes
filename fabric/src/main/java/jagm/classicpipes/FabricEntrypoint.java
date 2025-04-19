package jagm.classicpipes;

import net.fabricmc.api.ModInitializer;

public class FabricEntrypoint implements ModInitializer {

    @Override
    public void onInitialize() {
        ClassicPipes.LOGGER.info("Fabric mod started!");
    }

}
