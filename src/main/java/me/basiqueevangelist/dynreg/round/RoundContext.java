package me.basiqueevangelist.dynreg.round;

import me.basiqueevangelist.dynreg.network.EntryDescription;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface RoundContext {
    <T> T register(Identifier id, EntryDescription<T> desc);

    void removeEntry(Registry<?> registry, Identifier id);
}
