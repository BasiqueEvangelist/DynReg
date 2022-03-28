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
    public static Identifier START_TIMER = new Identifier("dynreg", "start_timer");
    public static Identifier RELOAD_RESOURCES = new Identifier("dynreg", "reload_resources");

    public static Packet<?> makeRoundFinishedPacket(List<RegistryKey<?>> removedEntries, Map<RegistryKey<?>, EntryDescription<?>> addedEntries) {
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeVarInt(removedEntries.size());
        for (var key : removedEntries) {
            buf.writeIdentifier(key.method_41185());
            buf.writeIdentifier(key.getValue());
        }

        buf.writeVarInt(addedEntries.size());
        for (var entry : addedEntries.entrySet()) {
            // The registry identifier isn't written, as the client can get it from the EntryDescription.
            buf.writeIdentifier(entry.getKey().getValue());

            buf.writeIdentifier(entry.getValue().id());
            entry.getValue().write(buf);
        }
        return ServerPlayNetworking.createS2CPacket(ROUND_FINISHED, buf);
    }

    public static Packet<?> makeResourceReloadPacket() {
        return ServerPlayNetworking.createS2CPacket(RELOAD_RESOURCES, PacketByteBufs.empty());
    }

    public static Packet<?> makeStartTimerPacket() {
        return ServerPlayNetworking.createS2CPacket(START_TIMER, PacketByteBufs.empty());
    }
}
