package me.basiqueevangelist.dynreg.compat.quilt;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.basiqueevangelist.dynreg.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.holder.ReactiveEntryTracker;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public final class QuiltRegistryCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/QuiltRegistryCompat");

    private static final @Nullable MethodHandle ID_SNAPSHOT_GETTER;

    static {
        MethodHandle handle = null;

        try {
            //noinspection JavaReflectionMemberAccess
            var f = SimpleRegistry.class.getDeclaredField("quilt$idSnapshot");
            f.setAccessible(true);
            handle = MethodHandles.lookup().unreflectGetter(f);
        } catch (Exception e) {
            LOGGER.error("Couldn't reflect into QSL to set quilt$idSnapshot, mod might be broken!");
        }

        ID_SNAPSHOT_GETTER = handle;
    }

    private QuiltRegistryCompat() {

    }

    @SuppressWarnings("unchecked")
    public static void init() {
        for (Registry<?> registry : Registry.REGISTRIES) {
            Event<RegistryEvents.EntryAdded<?>> event = (Event<RegistryEvents.EntryAdded<?>>) (Object) RegistryEvents.getEntryAddEvent(registry);

            event.addPhaseOrdering(ReactiveEntryTracker.START_PHASE, Event.DEFAULT_PHASE);
            event.addPhaseOrdering(Event.DEFAULT_PHASE, ReactiveEntryTracker.END_PHASE);

            event.register(ReactiveEntryTracker.START_PHASE, (ctx) -> {
                var entry = (RegistryEntry.Reference<?>) registry.getEntry(ctx.rawId()).orElseThrow();

                ReactiveEntryTracker.preObjectRegistered(registry, entry, false);
            });

            event.register(ReactiveEntryTracker.END_PHASE, (ctx) -> ReactiveEntryTracker.postObjectRegistered(registry));

            RegistryEntryDeletedCallback.event(registry).register((rawId, entry) -> {
                if (ID_SNAPSHOT_GETTER != null) {
                    try {
                        var snapshot = (ObjectList<RegistryEntry.Reference<?>>)
                            ID_SNAPSHOT_GETTER.invoke((SimpleRegistry<?>) registry);

                        if (snapshot != null) {
                            snapshot.removeIf(x -> x.getKey().equals(entry.getKey()));
                        }
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
}
