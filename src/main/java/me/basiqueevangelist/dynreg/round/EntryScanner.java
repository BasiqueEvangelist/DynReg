package me.basiqueevangelist.dynreg.round;

import me.basiqueevangelist.dynreg.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.holder.EntryData;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EntryScanner implements EntryScanContext {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/EntryScanner");

    private final Map<Identifier, EntryData> entryDatas = new HashMap<>();
    private final Map<RegistryKey<?>, List<EntryData>> pendingDeps = new HashMap<>();
    private final Map<RegistryKey<?>, EntryData> keyToEntry = new HashMap<>();
    private EntryData currentEntry = null;

    public EntryScanner(Collection<RegistrationEntry> entries, boolean isStartup) {
        for (RegistrationEntry entry : entries) {
            entryDatas.put(entry.id(), new EntryData(entry, isStartup));
        }
    }

    public Map<Identifier, EntryData> scan() {
        for (var entry : entryDatas.values()) {
            this.currentEntry = entry;

            try {
                entry.entry().scan(this);
            } catch (Exception e) {
                LOGGER.error("Encountered error while scanning {}", entry.entry().id(), e);
            }
        }

        if (!pendingDeps.isEmpty()) {
            LOGGER.warn("Entry scanner exited with pending dependencies");
        }

        return entryDatas;
    }

    @Override
    public ScanBuilder announce(Registry<?> registry, Identifier id) {
        return announce(RegistryKey.of(registry.getKey(), id));
    }

    @Override
    public ScanBuilder announce(RegistryKey<?> key) {
        this.keyToEntry.put(key, currentEntry);

        var pendingKeyDeps = pendingDeps.remove(key);

        if (pendingKeyDeps != null) {
            for (EntryData pendingEntry : pendingKeyDeps) {
                if (pendingEntry == currentEntry) continue;

                currentEntry.dependents().add(pendingEntry);
                pendingEntry.dependencies().add(currentEntry);
            }
        }

        return new Builder();
    }

    @Override
    public void announceDependency(Identifier entryId) {
        var dependency = entryDatas.get(entryId);

        if (dependency == null)
            throw new NoSuchElementException("Entry '" + entryId + "' doesn't exist");

        dependency.dependents().add(currentEntry);
        currentEntry.dependencies().add(dependency);
    }

    private class Builder implements ScanBuilder {
        @Override
        public ScanBuilder dependency(Registry<?> registry, Identifier id) {
            return dependency(RegistryKey.of(registry.getKey(), id));
        }

        @Override
        public ScanBuilder dependency(RegistryKey<?> key) {
            if (RegistryUtils.getRegistryOf(key).containsId(key.getValue()))
                return this;

            var otherEntry = keyToEntry.get(key);

            if (otherEntry != null) {
                if (otherEntry.equals(currentEntry)) return this;

                otherEntry.dependents().add(currentEntry);
                currentEntry.dependencies().add(otherEntry);
            } else {
                pendingDeps.computeIfAbsent(key, unused -> new ArrayList<>()).add(currentEntry);
            }

            return this;
        }
    }
}
