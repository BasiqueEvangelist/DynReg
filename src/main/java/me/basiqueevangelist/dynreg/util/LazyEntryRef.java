package me.basiqueevangelist.dynreg.util;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

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

    public T get() {
        if (instance == null) {
            instance = registry.get(id);
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
