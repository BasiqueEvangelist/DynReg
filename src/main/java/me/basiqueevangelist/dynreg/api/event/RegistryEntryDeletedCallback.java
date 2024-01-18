package me.basiqueevangelist.dynreg.api.event;

import me.basiqueevangelist.dynreg.api.RegistryModification;
import me.basiqueevangelist.dynreg.impl.access.ExtendedRegistry;
import net.fabricmc.fabric.api.event.AutoInvokingEvent;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

public interface RegistryEntryDeletedCallback<T> {
    /**
     * Called when an entry is {@linkplain RegistryModification#remove(RegistryKey) removed}
     * from the registry.
     *
     * @param rawId the entry's raw id
     * @param entry the entry
     */
    void onEntryDeleted(int rawId, RegistryEntry.Reference<T> entry);

    @SuppressWarnings("unchecked")
    @AutoInvokingEvent
    static <T> Event<RegistryEntryDeletedCallback<T>> event(Registry<T> registry) {
        return ((ExtendedRegistry<T>) registry).dynreg$getEntryDeletedEvent();
    }
}
