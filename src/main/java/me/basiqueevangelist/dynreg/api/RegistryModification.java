package me.basiqueevangelist.dynreg.api;

import me.basiqueevangelist.dynreg.access.ExtendedRegistry;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

/**
 * Low-level utilities for modifying registries.
 */
public final class RegistryModification {
    private RegistryModification() {

    }

    /**
     * Unfreezes a registry, allowing you to modify it.
     *
     * <p>
     *
     * @param registry the registry to unfreeze
     */
    public static void unfreeze(Registry<?> registry) {
        ((ExtendedRegistry<?>) registry).dynreg$unfreeze();
    }

    /**
     * Removes an entry from its registry.
     *
     * @param key the key of the entry to remove
     */
    @SuppressWarnings("unchecked")
    public static <T> void remove(RegistryKey<T> key) {
        ((ExtendedRegistry<T>) RegistryUtils.getRegistryOf(key)).dynreg$remove(key);
    }

    /**
     * Removes an entry from its registry.
     *
     * @param registry the registry to remove from
     * @param id the entry's id
     */
    @SuppressWarnings("unchecked")
    public static void remove(Registry<?> registry, Identifier id) {
        ((ExtendedRegistry<Object>) registry).dynreg$remove(RegistryKey.of((RegistryKey<? extends Registry<Object>>) registry.getKey(), id));
    }
}
