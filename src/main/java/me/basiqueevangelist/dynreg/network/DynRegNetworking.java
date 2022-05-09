package me.basiqueevangelist.dynreg.network;

import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class DynRegNetworking {
    private DynRegNetworking() {

    }

    public static Identifier ROUND_FINISHED = new Identifier("dynreg", "round_finished");
    public static Identifier START_TIMER = new Identifier("dynreg", "start_timer");
    public static Identifier STOP_TIMER = new Identifier("dynreg", "stop_timer");
    public static Identifier RELOAD_RESOURCES = new Identifier("dynreg", "reload_resources");

    public static Packet<?> makeRoundFinishedPacket(List<Identifier> removedEntries, Collection<RegistrationEntry> addedEntries) {
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeVarInt(removedEntries.size());
        for (var key : removedEntries) {
            buf.writeIdentifier(key);
        }

        buf.writeVarInt(addedEntries.size());
        for (var entry : addedEntries) {
            buf.writeIdentifier(entry.typeId());
            buf.writeIdentifier(entry.id());

            entry.write(buf);
        }
        return ServerPlayNetworking.createS2CPacket(ROUND_FINISHED, buf);
    }

    public static final Packet<?> RELOAD_RESOURCES_PACKET = ServerPlayNetworking.createS2CPacket(RELOAD_RESOURCES, PacketByteBufs.empty());
    public static final Packet<?> START_TIMER_PACKET = ServerPlayNetworking.createS2CPacket(START_TIMER, PacketByteBufs.empty());
    public static final Packet<?> STOP_TIMER_PACKET = ServerPlayNetworking.createS2CPacket(STOP_TIMER, PacketByteBufs.empty());
}
