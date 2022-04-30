package me.basiqueevangelist.dynreg.fixer;

import me.basiqueevangelist.dynreg.access.DeletableObjectInternal;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class GlobalFixer {
    private GlobalFixer() {

    }

    public static void init() {
        for (Registry<?> registry : Registry.REGISTRIES) {
            RegistryEntryAddedCallback.event(registry).register(GlobalFixer::onEntryAdded);
        }
    }

    private static void onEntryAdded(int rawId, Identifier id, Object obj) {
        if (obj instanceof DeletableObjectInternal doi)
            doi.dynreg$setDeleted(false);
    }
}
