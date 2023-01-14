package me.basiqueevangelist.dynreg.entry;

import io.netty.buffer.ByteBufUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.AbstractBlock;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;

// TODO: make this not use PacketByteBufs.
public class SimpleHashers {
    public static int hash(AbstractBlock.Settings settings) {
        PacketByteBuf buf = PacketByteBufs.create();
        SimpleSerializers.writeBlockSettings(buf, settings);
        return ByteBufUtil.hashCode(buf);
    }

    public static int hash(Item.Settings settings) {
        PacketByteBuf buf = PacketByteBufs.create();
        SimpleSerializers.writeItemSettings(buf, settings);
        return ByteBufUtil.hashCode(buf);
    }
}
