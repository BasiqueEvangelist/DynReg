package me.basiqueevangelist.dynreg.compat.quilt;

import me.basiqueevangelist.dynreg.holder.ReactiveEntryTracker;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;

public final class QuiltRegistryCompat {
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

            event.register(ReactiveEntryTracker.END_PHASE,
                (ctx) -> ReactiveEntryTracker.postObjectRegistered(registry));
        }
    }
}
