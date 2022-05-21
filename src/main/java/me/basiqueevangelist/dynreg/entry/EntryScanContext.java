package me.basiqueevangelist.dynreg.entry;

import me.basiqueevangelist.dynreg.util.LazyEntryRef;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public interface EntryScanContext {
    ScanBuilder announce(Registry<?> registry, Identifier id);

    ScanBuilder announce(RegistryKey<?> key);

    void announceDependency(Identifier entryId);

    interface ScanBuilder {
        ScanBuilder dependency(Registry<?> registry, Identifier id);

        ScanBuilder dependency(RegistryKey<?> key);

        default ScanBuilder dependency(LazyEntryRef<?> ref) {
            return dependency(ref.registry(), ref.id());
        }
    }
}
