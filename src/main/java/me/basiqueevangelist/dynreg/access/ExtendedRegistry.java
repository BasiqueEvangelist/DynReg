package me.basiqueevangelist.dynreg.access;

import me.basiqueevangelist.dynreg.event.RegistryEntryDeletedCallback;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.util.registry.RegistryKey;

public interface ExtendedRegistry<T> {
    Event<RegistryEntryDeletedCallback<T>> dynreg$getEntryDeletedEvent();

    void dynreg$remove(RegistryKey<T> key);

    void dynreg$unfreeze();
}
