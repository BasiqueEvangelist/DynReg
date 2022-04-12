package me.basiqueevangelist.dynreg.entry;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface EntryDescription<T> {
    T create();

    void write(PacketByteBuf buf);

    Registry<? super T> registry();

    default Identifier id() {
        return EntryDescriptions.getDescriptionId(this);
    }
}
