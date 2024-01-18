package me.basiqueevangelist.dynreg.impl.fixer;

import me.basiqueevangelist.dynreg.api.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.impl.util.DebugContext;
import me.basiqueevangelist.dynreg.impl.util.VersionTracker;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

public class StatusEffectFixer {
    public static final VersionTracker EFFECTS_VERSION = new VersionTracker();

    private StatusEffectFixer() {

    }

    public static void init() {
        RegistryEntryDeletedCallback.event(Registries.STATUS_EFFECT).register(StatusEffectFixer::onEffectDeleted);

        DebugContext.addSupplied("dynreg:effects_version", EFFECTS_VERSION::getVersion);
    }

    private static void onEffectDeleted(int rawId, RegistryEntry.Reference<StatusEffect> entry) {
        EFFECTS_VERSION.bumpVersion();
    }
}
