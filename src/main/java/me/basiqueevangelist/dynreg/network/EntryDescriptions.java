package me.basiqueevangelist.dynreg.network;

import me.basiqueevangelist.dynreg.DynReg;
import me.basiqueevangelist.dynreg.network.block.SimpleBlockDescription;
import me.basiqueevangelist.dynreg.network.item.BlockItemDescription;
import me.basiqueevangelist.dynreg.network.item.SimpleItemDescription;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class EntryDescriptions {
    private static final Map<Identifier, Function<PacketByteBuf, ? extends EntryDescription<?>>> DESCRIPTION_PARSERS = new HashMap<>();
    private static final Map<Class<?>, Identifier> DESCRIPTION_TYPES = new HashMap<>();

    private EntryDescriptions() {

    }

    public static Function<PacketByteBuf, ? extends EntryDescription<?>> getDescParser(Identifier id) {
        return DESCRIPTION_PARSERS.get(id);
    }

    public static Identifier getDescriptionId(EntryDescription<?> desc) {
        return DESCRIPTION_TYPES.get(desc.getClass());
    }

    @SafeVarargs
    public static <D extends EntryDescription<?>> void registerDescType(Identifier id, Function<PacketByteBuf, D> reader, D... classGetter) {
        if (DESCRIPTION_PARSERS.put(id, reader) != null)
            throw new IllegalStateException("Block description type " + id + " already registered!");

        Class<?> descType = classGetter.getClass().componentType();

        if (DESCRIPTION_TYPES.put(descType, id) != null)
            throw new IllegalStateException("Type " + descType.getName() + " was already registered as " + DESCRIPTION_TYPES.get(descType));
    }

    static {
        registerDescType(DynReg.id("simple_block"), SimpleBlockDescription::new);
        registerDescType(DynReg.id("simple_item"), SimpleItemDescription::new);
        registerDescType(DynReg.id("block_item"), BlockItemDescription::new);
    }
}
