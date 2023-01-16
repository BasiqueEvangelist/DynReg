package me.basiqueevangelist.dynreg.compat;

import me.basiqueevangelist.dynreg.compat.polymer.PolymerCompat;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.InvocationTargetException;

public final class CompatLoader {
    public static final boolean QUILT_BASE_LOADED = FabricLoader.getInstance().isModLoaded("quilt_base");
    public static final boolean POLYMER_LOADED = FabricLoader.getInstance().isModLoaded("polymer");

    private CompatLoader() {

    }

    public static void init() {
        if (QUILT_BASE_LOADED) {
            try {
                var klass = Class.forName("me.basiqueevangelist.dynreg.compat.quilt.QuiltCompatInit");
                klass.getMethod("init").invoke(null);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        if (POLYMER_LOADED)
            PolymerCompat.init();
    }
}
