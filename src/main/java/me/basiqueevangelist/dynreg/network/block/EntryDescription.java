package me.basiqueevangelist.dynreg.network.block;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface EntryDescription<T> {
    T create();

    void write(PacketByteBuf buf);

    Class<T> entryType();

    Registry<? super T> registry();

    Identifier id();
}
