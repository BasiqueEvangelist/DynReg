package me.basiqueevangelist.dynreg.network;

import me.basiqueevangelist.dynreg.DynReg;
import me.basiqueevangelist.dynreg.network.block.EntryDescription;
import me.basiqueevangelist.dynreg.network.block.SimpleBlockDescription;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class EntryDescriptions {
    private static Map<Identifier, Function<PacketByteBuf, EntryDescription<?>>> DESCRIPTION_TYPES = new HashMap<>();

    public static Identifier SIMPLE_BLOCK_ID = DynReg.id("simple_block");

    private EntryDescriptions() {

    }

    public static Function<PacketByteBuf, EntryDescription<?>> getDescParser(Identifier id) {
        return DESCRIPTION_TYPES.get(id);
    }

    public static void registerDescType(Identifier id, Function<PacketByteBuf, EntryDescription<?>> reader) {
        if (DESCRIPTION_TYPES.put(id, reader) != null)
            throw new IllegalStateException("Block description type " + id + " already registered!");
    }

    static {
        registerDescType(SIMPLE_BLOCK_ID, SimpleBlockDescription::new);
    }
}
