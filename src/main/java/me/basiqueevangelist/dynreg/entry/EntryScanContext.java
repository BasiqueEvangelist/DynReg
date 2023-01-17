package me.basiqueevangelist.dynreg.entry;

import me.basiqueevangelist.dynreg.util.LazyEntryRef;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface EntryScanContext {
    void announce(Registry<?> registry, Identifier id);

    void announce(AnnounceableResource resource);

    void dependency(Identifier entryId);
    void dependency(AnnounceableResource resource);
    void dependency(Registry<?> registry, Identifier id);
    default void dependency(LazyEntryRef<?> ref) {
        dependency(ref.registry(), ref.id());
    }
}
