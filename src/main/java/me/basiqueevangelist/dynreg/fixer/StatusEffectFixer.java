package me.basiqueevangelist.dynreg.fixer;

import me.basiqueevangelist.dynreg.debug.DebugContext;
import me.basiqueevangelist.dynreg.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.util.VersionTracker;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;

public class StatusEffectFixer {
    public static VersionTracker EFFECTS_VERSION = new VersionTracker();

    private StatusEffectFixer() {

    }

    public static void init() {
        RegistryEntryDeletedCallback.event(Registry.STATUS_EFFECT).register(StatusEffectFixer::onEffectDeleted);

        DebugContext.addSupplied("dynreg:effects_version", () -> EFFECTS_VERSION.getVersion());
    }

    private static void onEffectDeleted(int rawId, RegistryEntry.Reference<StatusEffect> entry) {
        EFFECTS_VERSION.bumpVersion();
    }
}
