package me.basiqueevangelist.dynreg.holder;

import me.basiqueevangelist.dynreg.entry.EntryRegisterContext;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.util.List;

public record EntryData(RegistrationEntry entry, List<RegistryKey<?>> registeredKeys) {
    public EntryRegisterContext createRegistrationContext() {
        return new Context();
    }

    private class Context implements EntryRegisterContext {
        @Override
        public <T> T register(Registry<? super T> registry, Identifier id, T item) {
            registeredKeys.add(RegistryKey.of(registry.getKey(), id));

            return Registry.register(registry, id, item);
        }
    }
}