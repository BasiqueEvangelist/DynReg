package me.basiqueevangelist.dynreg.api.entry;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public interface EntryRegisterContext {
    /**
     * Registers an entry, marking it as part of this entry.
     */
    <T> T register(Registry<? super T> registry, Identifier id, T item);
}
