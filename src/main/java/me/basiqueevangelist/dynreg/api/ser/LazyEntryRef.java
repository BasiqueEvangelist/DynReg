package me.basiqueevangelist.dynreg.api.ser;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * A lazily resolved reference to a registry entry
 * @param <T> the type of the entry
 */
public class LazyEntryRef<T> {
    private final Registry<T> registry;
    private final Identifier id;

    private T instance;

    public LazyEntryRef(Registry<T> registry, Identifier id) {
        this.registry = registry;
        this.id = id;
    }

    public static <T> LazyEntryRef<T> read(PacketByteBuf buf, Registry<T> registry) {
        return new LazyEntryRef<>(registry, buf.readIdentifier());
    }

    public Registry<T> registry() {
        return registry;
    }

    public Identifier id() {
        return id;
    }

    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(id);
    }

    /**
     * Resolves and gets the registry entry.
     *
     * @return the referenced registry entry
     * @throws IllegalStateException if the registry entry doesn't exist
     */
    public T get() {
        if (instance == null) {
            instance = registry.get(id);

            if (instance == null) {
                throw new IllegalStateException("'" + registry.getKey().getValue() + "' doesn't have an entry with id '" + id + "'");
            }
        }

        return instance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LazyEntryRef<?> that = (LazyEntryRef<?>) o;

        if (!registry.equals(that.registry)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        int result = registry.getKey().getValue().hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }
}
