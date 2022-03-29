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
    public static <T> void remove(RegistryKey<T> key) {
        ((ExtendedRegistry<T>) getRegistryOf(key)).dynreg$remove(key);
    }

    @SuppressWarnings("unchecked")
    public static void remove(Registry<?> registry, Identifier id) {
        ((ExtendedRegistry<Object>) registry).dynreg$remove(RegistryKey.of((RegistryKey<? extends Registry<Object>>) registry.getKey(), id));
    }

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> getRegistryOf(RegistryKey<T> key) {
        return (Registry<T>) Registry.REGISTRIES.getOrEmpty(key.method_41185()).orElseThrow();
    }
}
