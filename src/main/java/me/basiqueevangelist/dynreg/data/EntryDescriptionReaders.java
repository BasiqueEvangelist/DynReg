package me.basiqueevangelist.dynreg.data;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.DynReg;
import me.basiqueevangelist.dynreg.network.EntryDescription;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class EntryDescriptionReaders {
    private static final Map<Identifier, Function<JsonObject, EntryDescription<?>>> READERS = new HashMap<>();

    private EntryDescriptionReaders() {

    }

    public static Function<JsonObject, EntryDescription<?>> getReader(Identifier id) {
        return READERS.get(id);
    }

    public static void register(Identifier id, Function<JsonObject, EntryDescription<?>> reader) {
        READERS.put(id, reader);
    }

    static {
        register(DynReg.id("simple_block"), SimpleReaders::readSimpleBlock);
        register(DynReg.id("simple_item"), SimpleReaders::readSimpleItem);
        register(DynReg.id("block_item"), SimpleReaders::readBlockItem);
    }
}
