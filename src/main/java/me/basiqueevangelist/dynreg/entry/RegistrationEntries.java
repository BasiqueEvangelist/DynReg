package me.basiqueevangelist.dynreg.entry;

import me.basiqueevangelist.dynreg.DynReg;
import me.basiqueevangelist.dynreg.api.entry.RegistrationEntry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class RegistrationEntries {
    private static final Map<Identifier, BiFunction<Identifier, PacketByteBuf, ? extends RegistrationEntry>> ENTRY_DESERIALIZERS = new HashMap<>();
    private static final Map<Class<?>, Identifier> ENTRY_TYPES = new HashMap<>();

    private RegistrationEntries() {

    }

    public static BiFunction<Identifier, PacketByteBuf, ? extends RegistrationEntry> getEntryDeserializer(Identifier id) {
        return ENTRY_DESERIALIZERS.get(id);
    }

    public static Identifier getEntryId(RegistrationEntry desc) {
        return ENTRY_TYPES.get(desc.getClass());
    }

    @SafeVarargs
    public static <D extends RegistrationEntry> void registerEntryType(Identifier id, BiFunction<Identifier, PacketByteBuf, D> reader, D... classGetter) {
        if (ENTRY_DESERIALIZERS.put(id, reader) != null)
            throw new IllegalStateException("Registration entry type " + id + " is already registered!");

        Class<?> descType = classGetter.getClass().componentType();

        if (ENTRY_TYPES.put(descType, id) != null)
            throw new IllegalStateException("Type " + descType.getName() + " was already registered as " + ENTRY_TYPES.get(descType));
    }

    static {
        registerEntryType(DynReg.id("simple_block"), SimpleBlockEntry::new);
        registerEntryType(DynReg.id("simple_item"), SimpleItemEntry::new);
    }
}
