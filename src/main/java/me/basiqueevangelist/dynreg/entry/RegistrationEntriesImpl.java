package me.basiqueevangelist.dynreg.entry;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.DynReg;
import me.basiqueevangelist.dynreg.api.entry.RegistrationEntries;
import me.basiqueevangelist.dynreg.api.entry.RegistrationEntry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class RegistrationEntriesImpl {
    private static final Map<Identifier, NetworkData<?>> NETWORK_DATA = new HashMap<>();
    private static final Map<Identifier, BiFunction<Identifier, JsonObject, ? extends RegistrationEntry>> JSON_READERS = new HashMap<>();
    private static final Map<Class<?>, Identifier> TYPES = new HashMap<>();

    public static <E extends RegistrationEntry> RegistrationEntries.Registration<E> register(Identifier id, Class<E> klass) {
        if (TYPES.put(klass, id) != null)
            throw new IllegalStateException("Registration entry type " + id + " is already registered!");

        return new RegistrationImpl<>(id, klass);
    }

    public static Identifier getEntryId(RegistrationEntry desc) {
        return TYPES.get(desc.getClass());
    }

    public static NetworkData<? extends RegistrationEntry> getNetworkData(Identifier id) {
        return NETWORK_DATA.get(id);
    }

    @SuppressWarnings("unchecked")
    public static <E extends RegistrationEntry> NetworkData<E> getNetworkData(E entry) {
        return (NetworkData<E>) NETWORK_DATA.get(entry.typeId());
    }

    public static BiFunction<Identifier, JsonObject, ? extends RegistrationEntry> getReader(Identifier id) {
        return JSON_READERS.get(id);
    }


    public record RegistrationImpl<E extends RegistrationEntry>(Identifier id, Class<E> klass) implements RegistrationEntries.Registration<E> {
        public RegistrationEntries.Registration<E> network(BiConsumer<E, PacketByteBuf> serializer, BiFunction<Identifier, PacketByteBuf, E> deserializer) {
            NETWORK_DATA.put(id, new NetworkData<>(serializer, deserializer));
            return this;
        }

        @Override
        public RegistrationEntries.Registration<E> json(BiFunction<Identifier, JsonObject, E> reader) {
            JSON_READERS.put(id, reader);
            return this;
        }
    }

    public record NetworkData<E extends RegistrationEntry>(BiConsumer<E, PacketByteBuf> serializer, BiFunction<Identifier, PacketByteBuf, E> deserializer) { }

    static {
        register(DynReg.id("simple_block"), SimpleBlockEntry.class)
            .network(SimpleBlockEntry::write, SimpleBlockEntry::new)
            .json(SimpleBlockEntry::new);

        register(DynReg.id("simple_item"), SimpleItemEntry.class)
            .network(SimpleItemEntry::write, SimpleItemEntry::new)
            .json(SimpleItemEntry::new);
    }
}