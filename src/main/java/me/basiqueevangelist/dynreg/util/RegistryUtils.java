package me.basiqueevangelist.dynreg.util;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public final class RegistryUtils {
    private RegistryUtils() {

    }

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> getRegistryOf(RegistryKey<T> key) {
        return (Registry<T>) Registries.REGISTRIES.getOrEmpty(key.getRegistry()).orElseThrow();
    }

    @SuppressWarnings("unchecked")
    public static int getRawIdOfRegistryOf(RegistryKey<?> key) {
        return ((Registry<Registry<?>>) Registries.REGISTRIES).getRawId(getRegistryOf(key));
    }
}
