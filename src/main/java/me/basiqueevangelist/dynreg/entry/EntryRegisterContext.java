package me.basiqueevangelist.dynreg.entry;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public interface EntryRegisterContext {
    <T> T register(Registry<? super T> registry, Identifier id, T item);
}
