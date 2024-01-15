package me.basiqueevangelist.dynreg.access;

import me.basiqueevangelist.dynreg.api.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.api.event.RegistryFrozenCallback;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.registry.RegistryKey;

public interface ExtendedRegistry<T> {
    Event<RegistryEntryDeletedCallback<T>> dynreg$getEntryDeletedEvent();

    Event<RegistryFrozenCallback<T>> dynreg$getRegistryFrozenEvent();

    void dynreg$remove(RegistryKey<T> key);

    void dynreg$unfreeze();

    void dynreg$installStackTracingMap();
}
