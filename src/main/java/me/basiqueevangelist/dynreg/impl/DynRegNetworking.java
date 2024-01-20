package me.basiqueevangelist.dynreg.impl;

import me.basiqueevangelist.dynreg.api.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.impl.entry.RegistrationEntriesImpl;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

import java.util.Collection;

public final class DynRegNetworking {
    private DynRegNetworking() {

    }

    public static Identifier ROUND_FINISHED = new Identifier("dynreg", "round_finished");
    public static Identifier ROUND_SYNC_COMPLETE = new Identifier("dynreg", "round_sync_complete");

    public static Packet<?> makeRoundFinishedPacket(long hash, boolean reloadResources,
                                                    Collection<RegistrationEntry> addedEntries) {
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeLong(hash);
        buf.writeBoolean(reloadResources);

        buf.writeVarInt(addedEntries.size());
        for (var entry : addedEntries) {
            buf.writeIdentifier(entry.typeId());
            buf.writeIdentifier(entry.id());

            RegistrationEntriesImpl.getNetworkData(entry).serializer().accept(entry, buf);
        }
        return ServerPlayNetworking.createS2CPacket(ROUND_FINISHED, buf);
    }
}
