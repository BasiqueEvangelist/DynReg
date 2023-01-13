package me.basiqueevangelist.dynreg.network;

import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.Collection;

public final class DynRegNetworking {
    private DynRegNetworking() {

    }

    public static Identifier ROUND_FINISHED = new Identifier("dynreg", "round_finished");

    public static Packet<?> makeRoundFinishedPacket(Collection<Identifier> removedEntries, Collection<RegistrationEntry> addedEntries) {
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
}
