package me.basiqueevangelist.dynreg.network;

import me.basiqueevangelist.dynreg.network.block.EntryDescription;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;

import java.util.List;
import java.util.Map;

public final class DynRegNetworking {
    private DynRegNetworking() {

    }

    public static Identifier ROUND_FINISHED = new Identifier("dynreg", "round_finished");
    public static Identifier RELOAD_RESOURCES = new Identifier("dynreg", "reload_resources");

    public static Packet<?> makeRoundFinishedPacket(List<RegistryKey<?>> removedEntries, Map<Identifier, EntryDescription> blocks) {
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeCollection(removedEntries, (buf2, key) -> {
            buf2.writeIdentifier(key.method_41185());
            buf2.writeIdentifier(key.getValue());
        });

        buf.writeMap(blocks, PacketByteBuf::writeIdentifier, (buf2, desc) -> {
            buf2.writeIdentifier(desc.id());
            desc.write(buf2);
        });

        return ServerPlayNetworking.createS2CPacket(ROUND_FINISHED, buf);
    }

    public static Packet<?> makeResourceReloadPacket() {
        return ServerPlayNetworking.createS2CPacket(RELOAD_RESOURCES, PacketByteBufs.empty());
    }
}
