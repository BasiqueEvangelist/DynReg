package me.basiqueevangelist.dynreg.access;

import net.minecraft.network.PacketByteBuf;

public interface InternalWritable {
    void dynreg$write(PacketByteBuf buf);
}
