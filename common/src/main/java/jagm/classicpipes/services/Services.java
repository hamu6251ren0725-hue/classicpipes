package jagm.classicpipes.services;

import jagm.classicpipes.ClassicPipes;

import java.util.ServiceLoader;

public class Services {

    public static final LoaderService LOADER_SERVICE = load(LoaderService.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new NullPointerException(("Failed to load service for " + clazz.getName())));
        ClassicPipes.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }

}
