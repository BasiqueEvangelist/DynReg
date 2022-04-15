package me.basiqueevangelist.dynreg.compat;

import me.basiqueevangelist.dynreg.compat.qsl.QuiltRegistryCompat;
import net.fabricmc.loader.api.FabricLoader;

public final class CompatLoader {
    public static final boolean QUILT_REGISTRY_LOADED = FabricLoader.getInstance().isModLoaded("quilt_registry");

    private CompatLoader() {

    }

    public static void init() {
        if (QUILT_REGISTRY_LOADED)
            QuiltRegistryCompat.init();
    }
}
