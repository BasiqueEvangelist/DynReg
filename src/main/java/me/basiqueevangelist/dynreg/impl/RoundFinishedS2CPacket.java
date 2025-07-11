package me.basiqueevangelist.dynreg.impl;

import me.basiqueevangelist.dynreg.api.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.impl.entry.RegistrationEntriesImpl;
import me.basiqueevangelist.dynreg.impl.holder.LoadedEntryHolder;
import me.basiqueevangelist.dynreg.impl.round.ModificationRoundImpl;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record RoundFinishedS2CPacket(long hash, boolean reloadResources, Collection<RegistrationEntry> addedEntries) implements CustomPayload {
    public static final Id<RoundFinishedS2CPacket> ID = new Id<>(DynReg.id("round_finished"));
    public static final PacketCodec<PacketByteBuf, RoundFinishedS2CPacket> PACKET_CODEC = new PacketCodec<>() {
        @Override
        public RoundFinishedS2CPacket decode(PacketByteBuf buf) {
            return read(buf);
        }

        @Override
        public void encode(PacketByteBuf buf, RoundFinishedS2CPacket value) {
            value.write(buf);
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static RoundFinishedS2CPacket read(PacketByteBuf buf) {
        long hash = buf.readLong();
        boolean reloadResources = buf.readBoolean();

        int addedEntriesCount = buf.readVarInt();
        List<RegistrationEntry> addedEntries = new ArrayList<>();

        for (int i = 0; i < addedEntriesCount; i++) {
            Identifier typeId = buf.readIdentifier();
            Identifier entryId = buf.readIdentifier();

            RegistrationEntry entry = RegistrationEntriesImpl.getNetworkData(typeId).deserializer().apply(entryId, buf);

            addedEntries.add(entry);
        }

        return new RoundFinishedS2CPacket(hash, reloadResources, addedEntries);
    }

    public void write(PacketByteBuf buf) {
        buf.writeLong(hash);
        buf.writeBoolean(reloadResources);

        buf.writeVarInt(addedEntries.size());
        for (var entry : addedEntries) {
            buf.writeIdentifier(entry.typeId());
            buf.writeIdentifier(entry.id());

            RegistrationEntriesImpl.getNetworkData(entry).serializer().accept(entry, buf);
        }
    }
}
