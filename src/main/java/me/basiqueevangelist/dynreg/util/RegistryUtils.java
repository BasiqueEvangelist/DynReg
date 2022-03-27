package me.basiqueevangelist.dynreg.util;

import me.basiqueevangelist.dynreg.access.ExtendedRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public final class RegistryUtils {
    private RegistryUtils() {

    }

    public static void unfreeze(Registry<?> registry) {
        ((ExtendedRegistry<?>) registry).dynreg$unfreeze();
    }

    @SuppressWarnings("unchecked")
    public static <T> void remove(Registry<T> registry, RegistryKey<T> key) {
        ((ExtendedRegistry<T>) registry).dynreg$remove(key);
    }

    @SuppressWarnings("unchecked")
    public static void remove(Registry<?> registry, Identifier id) {
        remove((Registry<Object>) registry, (RegistryKey<Object>) RegistryKey.of(registry.getKey(), id));
    }
}
