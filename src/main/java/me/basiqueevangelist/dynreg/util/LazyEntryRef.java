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
}
