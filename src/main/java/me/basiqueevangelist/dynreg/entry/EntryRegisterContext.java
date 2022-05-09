package me.basiqueevangelist.dynreg.entry;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface EntryRegisterContext {
    <T> T register(Registry<? super T> registry, Identifier id, T item);
}
