package me.basiqueevangelist.dynreg.holder;

import me.basiqueevangelist.dynreg.DynReg;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ReactiveEntryTracker {
    public static final Identifier START_PHASE = DynReg.id("reactive_start");
    public static final Identifier END_PHASE = DynReg.id("reactive_end");
    private static final Map<Registry<?>, List<RegistryEntry.Reference<?>>> STACKS = new HashMap<>();

    private ReactiveEntryTracker() {

    }

    @SuppressWarnings("unchecked")
    public static void init() {
        for (Registry<?> registry : Registry.REGISTRIES) {
            Event<RegistryEntryAddedCallback<?>> event = (Event<RegistryEntryAddedCallback<?>>) (Object) RegistryEntryAddedCallback.event(registry);

            event.addPhaseOrdering(START_PHASE, Event.DEFAULT_PHASE);
            event.addPhaseOrdering(Event.DEFAULT_PHASE, END_PHASE);

            event.register(START_PHASE, (rawId, id, object) -> {
                var entry = (RegistryEntry.Reference<?>) registry.getEntry(rawId).orElse(null);

                if (entry != null)
                    preObjectRegistered(registry, entry, true);
            });

            event.register(END_PHASE, (rawId, id, object) -> postObjectRegistered(registry));
        }
    }

    public static void preObjectRegistered(Registry<?> registry, RegistryEntry.Reference<?> newEntry, boolean isPrimary) {
        List<RegistryEntry.Reference<?>> currentStack = STACKS.computeIfAbsent(registry, unused -> new ArrayList<>());

        if (!currentStack.isEmpty() && isPrimary) {
            DependencyManager.addDependency(newEntry, currentStack.get(currentStack.size() - 1));
        }

        currentStack.add(newEntry);
    }

    public static void postObjectRegistered(Registry<?> registry) {
        List<RegistryEntry.Reference<?>> currentStack = STACKS.computeIfAbsent(registry, unused -> new ArrayList<>());

        if (currentStack.size() > 0)
            currentStack.remove(currentStack.size() - 1);
    }
}
