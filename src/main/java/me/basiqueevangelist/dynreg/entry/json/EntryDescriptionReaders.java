package me.basiqueevangelist.dynreg.entry.json;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.DynReg;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.entry.block.SimpleBlockEntry;
import me.basiqueevangelist.dynreg.entry.item.SimpleItemEntry;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class EntryDescriptionReaders {
    private static final Map<Identifier, BiFunction<Identifier, JsonObject, RegistrationEntry>> READERS = new HashMap<>();

    private EntryDescriptionReaders() {

    }

    public static BiFunction<Identifier, JsonObject, RegistrationEntry> getReader(Identifier id) {
        return READERS.get(id);
    }

    public static void register(Identifier id, BiFunction<Identifier, JsonObject, RegistrationEntry> reader) {
        READERS.put(id, reader);
    }

    static {
        register(DynReg.id("simple_block"), SimpleBlockEntry::new);
        register(DynReg.id("simple_item"), SimpleItemEntry::new);
    }
}
