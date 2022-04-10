package me.basiqueevangelist.dynreg.round;

import me.basiqueevangelist.dynreg.network.EntryDescription;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public interface RoundContext {
    <T> T register(Identifier id, EntryDescription<T> desc);

    void removeEntry(RegistryKey<?> key);

    void removeEntry(Registry<?> registry, Identifier id);
}
