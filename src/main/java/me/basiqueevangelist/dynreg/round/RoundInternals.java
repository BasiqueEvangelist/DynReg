package me.basiqueevangelist.dynreg.round;

import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.holder.EntryData;
import me.basiqueevangelist.dynreg.holder.LoadedEntryHolder;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class RoundInternals {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/RoundInternals");

    public static void registerEntry(RegistrationEntry entry) {
        var data = new EntryData(entry, new ArrayList<>());

        entry.register(data.createRegistrationContext());

        LoadedEntryHolder.addEntry(data);
    }

    public static void removeEntry(Identifier entryId) {
        var entry = LoadedEntryHolder.getEntries().get(entryId);

        if (entry == null) {
            LOGGER.warn("Removed entry {} was already removed", entryId);
            return;
        }

        entry.entry().onRemoved();

        for (RegistryKey<?> registeredKey : entry.registeredKeys()) {
            RegistryUtils.remove(registeredKey);
        }

        LoadedEntryHolder.removeEntry(entryId);
    }
}
