package me.basiqueevangelist.dynreg.compat;

import me.basiqueevangelist.dynreg.compat.polymer.PolymerCompat;
import me.basiqueevangelist.dynreg.compat.qsl.QuiltRegistryCompat;
import net.fabricmc.loader.api.FabricLoader;

public final class CompatLoader {
    public static final boolean QUILT_REGISTRY_LOADED = FabricLoader.getInstance().isModLoaded("quilt_registry");
    public static final boolean POLYMER_LOADED = FabricLoader.getInstance().isModLoaded("polymer");

    private CompatLoader() {

    }

    public static void init() {
        if (QUILT_REGISTRY_LOADED)
            QuiltRegistryCompat.init();

        if (POLYMER_LOADED)
            PolymerCompat.init();
    }
}
