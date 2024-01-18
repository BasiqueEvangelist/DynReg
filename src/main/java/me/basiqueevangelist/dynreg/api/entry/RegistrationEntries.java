package me.basiqueevangelist.dynreg.api.entry;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.impl.entry.RegistrationEntriesImpl;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public final class RegistrationEntries {
    private RegistrationEntries() {

    }

    /**
     * Registers a registration entry type.
     *
     * <p>The entry will initially be fully non-serializable. To customize it, use the methods on {@link Registration}.
     *
     * @param id the entry's id
     * @param klass the type of the entry
     */
    public static <E extends RegistrationEntry> Registration<E> register(Identifier id, Class<E> klass) {
        return RegistrationEntriesImpl.register(id, klass);
    }

    public interface Registration<E extends RegistrationEntry> {
        Registration<E> network(BiConsumer<E, PacketByteBuf> serializer, BiFunction<Identifier, PacketByteBuf, E> deserializer);

        Registration<E> json(BiFunction<Identifier, JsonObject, E> reader);
    }
}
