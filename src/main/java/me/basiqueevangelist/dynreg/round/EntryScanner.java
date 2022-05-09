package me.basiqueevangelist.dynreg.round;

import me.basiqueevangelist.dynreg.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EntryScanner implements EntryScanContext {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/EntryScanner");

    private Collection<RegistrationEntry> entries;
    private final Map<Identifier, Set<Identifier>> dependents = new HashMap<>();
    private final Map<RegistryKey<?>, List<Identifier>> pendingDeps = new HashMap<>();
    private final Map<RegistryKey<?>, Identifier> keyToEntry = new HashMap<>();
    private Identifier currentEntry = null;

    public EntryScanner(Collection<RegistrationEntry> entries) {
        this.entries = entries;
    }

    public Map<Identifier, Set<Identifier>> scan() {
        for (var entry : entries) {
            this.currentEntry = entry.id();

            entry.scan(this);
        }

        if (!pendingDeps.isEmpty()) {
            LOGGER.warn("Entry scanner");
        }

        return dependents;
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
            for (Identifier pendingEntry : pendingKeyDeps) {
                if (pendingEntry.equals(currentEntry)) continue;

                dependents.computeIfAbsent(currentEntry, unused -> new HashSet<>()).add(pendingEntry);
            }
        }

        return new Builder(key);
    }

    @Override
    public void announceDependency(Identifier entryId) {
        dependents.computeIfAbsent(entryId, unused -> new HashSet<>()).add(currentEntry);
    }

    private class Builder implements ScanBuilder {
        private final RegistryKey<?> key;

        public Builder(RegistryKey<?> key) {
            this.key = key;
        }

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

                dependents.computeIfAbsent(otherEntry, unused -> new HashSet<>()).add(currentEntry);
            } else {
                pendingDeps.computeIfAbsent(key, unused -> new ArrayList<>()).add(currentEntry);
            }

            return this;
        }
    }
}
