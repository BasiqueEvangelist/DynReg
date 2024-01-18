package me.basiqueevangelist.dynreg.compat.quilt;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.basiqueevangelist.dynreg.api.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.impl.holder.ReactiveEntryTracker;
import me.basiqueevangelist.dynreg.impl.util.ReflectionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;

import java.lang.invoke.MethodHandle;

public final class QuiltRegistryCompat {
    private static final @Nullable MethodHandle ID_SNAPSHOT_GETTER = ReflectionUtil.findFieldWith(
        SimpleRegistry.class,
        "quilt$idSnapshot",
        "Couldn't reflect into QSL to set quilt$idSnapshot, mod might be broken!"
    );

    private QuiltRegistryCompat() {

    }

    @SuppressWarnings("unchecked")
    public static void init() {
        for (Registry<?> registry : Registries.REGISTRIES) {
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
