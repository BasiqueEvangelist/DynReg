package me.basiqueevangelist.dynreg.event;

import me.basiqueevangelist.dynreg.access.ExtendedRegistry;
import net.fabricmc.fabric.api.event.AutoInvokingEvent;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;

public interface RegistryEntryDeletedCallback<T> {
    void onEntryDeleted(int rawId, RegistryEntry.Reference<T> entry);

    @SuppressWarnings("unchecked")
    @AutoInvokingEvent
    static <T> Event<RegistryEntryDeletedCallback<T>> event(Registry<T> registry) {
        return ((ExtendedRegistry<T>) registry).dynreg$getEntryDeletedEvent();
    }
}
