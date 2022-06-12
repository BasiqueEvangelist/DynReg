package me.basiqueevangelist.dynreg.fixer;

import me.basiqueevangelist.dynreg.access.DeletableObjectInternal;
import me.basiqueevangelist.dynreg.event.RegistryEntryDeletedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;

public final class GlobalFixer<T> {
    private final Registry<T> registry;

    private GlobalFixer(Registry<T> registry) {
        this.registry = registry;
    }

    public static void init() {
        for (Registry<?> registry : Registry.REGISTRIES) {
            new GlobalFixer<>(registry).register();
        }
    }

    private void register() {
        RegistryEntryAddedCallback.event(registry).register(this::onEntryAdded);
        RegistryEntryDeletedCallback.event(registry).register(this::onEntryDeleted);
    }

    private void onEntryDeleted(int rawId, RegistryEntry.Reference<?> entry) {
        ((DeletableObjectInternal) entry).dynreg$setDeleted(true);

        if (entry.value() instanceof DeletableObjectInternal obj)
            obj.dynreg$setDeleted(true);
    }

    private void onEntryAdded(int rawId, Identifier id, Object obj) {
        ((DeletableObjectInternal) registry.getEntry(rawId).orElseThrow()).dynreg$setDeleted(false);

        if (obj instanceof DeletableObjectInternal doi)
            doi.dynreg$setDeleted(false);
    }
}
