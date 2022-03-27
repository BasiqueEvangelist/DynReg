package me.basiqueevangelist.dynreg.access;

import net.minecraft.network.PacketByteBuf;

public interface ExtendedBlockSettings {
    void dynreg$write(PacketByteBuf buf);
}
