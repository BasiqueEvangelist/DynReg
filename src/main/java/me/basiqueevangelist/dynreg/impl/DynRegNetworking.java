package me.basiqueevangelist.dynreg.impl;

import me.basiqueevangelist.dynreg.api.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.impl.entry.RegistrationEntriesImpl;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

import java.util.Collection;

public final class DynRegNetworking {
    private DynRegNetworking() {

    }

    public static Packet<?> makeRoundFinishedPacket(long hash, boolean reloadResources,
                                                    Collection<RegistrationEntry> addedEntries) {
        return ServerConfigurationNetworking.createS2CPacket(new RoundFinishedS2CPacket(hash, reloadResources, addedEntries));
    }
}
