package me.basiqueevangelist.dynreg.impl.fixer;

import me.basiqueevangelist.dynreg.api.event.RoundEvents;
import me.basiqueevangelist.dynreg.impl.DynReg;
import me.basiqueevangelist.dynreg.impl.util.ReflectionUtil;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.invoke.MethodHandle;

public final class FabricGlobalFixer {
    private static final MethodHandle SAVE_REGISTRY_DATA = ReflectionUtil.findMixinMethodWith(
        LevelStorage.Session.class,
        "net.fabricmc.fabric.mixin.registry.sync.LevelStorageSessionMixin",
        "fabric_saveRegistryData",
        "Couldn't reflect into FAPI to call fabric_saveRegistryData, mod might be broken!"
    );
    private static final MethodHandle READ_REGISTRY_DATA = ReflectionUtil.findMixinMethodWith(
        LevelStorage.Session.class,
        "net.fabricmc.fabric.mixin.registry.sync.LevelStorageSessionMixin",
        "readWorldProperties",
        "Couldn't reflect into FAPI to call readWorldProperties, mod might be broken!"
    );

    private FabricGlobalFixer() {

    }

    public static void init() {
        RoundEvents.PRE.register(FabricGlobalFixer::preRound);
        RoundEvents.POST.register(FabricGlobalFixer::postRound);
    }

    private static void preRound() {
        var server = DynReg.SERVER;

        if (server == null) return;
        if (SAVE_REGISTRY_DATA == null) return;

        var session = server.session;

        try {
            SAVE_REGISTRY_DATA.invoke(session);
        } catch (Throwable t) {
            throw new RuntimeException("Couldn't resave persisted registry data", t);
        }
    }

    private static void postRound() {
        var server = DynReg.SERVER;

        if (server == null) return;

        var session = server.session;
        if (READ_REGISTRY_DATA == null) return;

        try {
            READ_REGISTRY_DATA.invoke(session, (CallbackInfoReturnable<?>) null);
        } catch (Throwable t) {
            throw new RuntimeException("Couldn't reload persisted registry data", t);
        }
    }
}
