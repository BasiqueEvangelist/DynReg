package me.basiqueevangelist.dynreg.holder;

import me.basiqueevangelist.dynreg.entry.EntryRegisterContext;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record EntryData(RegistrationEntry entry, List<RegistryKey<?>> registeredKeys, List<EntryData> dependencies,
                        List<EntryData> dependents, boolean isStartup) {
    public EntryRegisterContext createRegistrationContext() {
        return new Context();
    }

    public EntryData(RegistrationEntry entry, boolean isStartup) {
        this(entry, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), isStartup);
    }

    private class Context implements EntryRegisterContext {
        @Override
        public <T> T register(Registry<? super T> registry, Identifier id, T item) {
            registeredKeys.add(RegistryKey.of(registry.getKey(), id));

            return Registry.register(registry, id, item);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntryData entryData = (EntryData) o;

        return entry.equals(entryData.entry);
    }

    @Override
    public int hashCode() {
        return entry.hashCode();
    }

    @Override
    public String toString() {
        return "EntryData[" + entry + "]";
    }
}
