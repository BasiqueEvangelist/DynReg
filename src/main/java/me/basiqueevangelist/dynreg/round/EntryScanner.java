package me.basiqueevangelist.dynreg.round;

import me.basiqueevangelist.dynreg.entry.AnnounceableResource;
import me.basiqueevangelist.dynreg.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.holder.EntryData;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EntryScanner implements EntryScanContext {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/EntryScanner");

    private final Map<Identifier, EntryData> entryDatas = new HashMap<>();
    private final Map<AnnounceableResource, List<EntryData>> pendingDeps = new HashMap<>();
    private final Map<AnnounceableResource, EntryData> resToEntry = new HashMap<>();
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
    public void announce(Registry<?> registry, Identifier id) {
        announce(RegistryKey.of(registry.getKey(), id));
    }

    @Override
    public void announce(AnnounceableResource res) {
        this.resToEntry.put(res, currentEntry);

        var pendingKeyDeps = pendingDeps.remove(res);

        if (pendingKeyDeps != null) {
            for (EntryData pendingEntry : pendingKeyDeps) {
                if (pendingEntry == currentEntry) continue;

                currentEntry.dependents().add(pendingEntry);
                pendingEntry.dependencies().add(currentEntry);
            }
        }
    }

    @Override
    public void dependency(Identifier entryId) {
        var dependency = entryDatas.get(entryId);

        if (dependency == null)
            throw new NoSuchElementException("Entry '" + entryId + "' doesn't exist");

        dependency.dependents().add(currentEntry);
        currentEntry.dependencies().add(dependency);
    }

    @Override
    public void dependency(Registry<?> registry, Identifier id) {
        dependency(RegistryKey.of(registry.getKey(), id));
    }

    @Override
    public void dependency(AnnounceableResource resource) {
        if (resource.dynreg$isAlreadyPresent())
            return;

        var otherEntry = resToEntry.get(resource);

        if (otherEntry != null) {
            if (otherEntry.equals(currentEntry)) return;

            otherEntry.dependents().add(currentEntry);
            currentEntry.dependencies().add(otherEntry);
        } else {
            pendingDeps.computeIfAbsent(resource, unused -> new ArrayList<>()).add(currentEntry);
        }
    }
}
