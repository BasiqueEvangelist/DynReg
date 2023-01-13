package me.basiqueevangelist.dynreg.holder;

import me.basiqueevangelist.dynreg.DynReg;
import me.basiqueevangelist.dynreg.network.DynRegNetworking;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class LoadedEntryHolder {
    private static final Map<Identifier, EntryData> ADDED_ENTRIES = new LinkedHashMap<>();
    private static final Identifier ROUND_SYNC_PHASE = DynReg.id("round_sync");

    private LoadedEntryHolder() {

    }

    public static void init() {
        ServerPlayConnectionEvents.JOIN.addPhaseOrdering(ROUND_SYNC_PHASE, Event.DEFAULT_PHASE);
        ServerPlayConnectionEvents.JOIN.register(ROUND_SYNC_PHASE, (handler, sender, server) -> {
            if (ADDED_ENTRIES.size() == 0) return;

            boolean isHost = server.isHost(handler.player.getGameProfile());

            if (!isHost) {
                // FIX: make this less stream-y.
                Packet<?> packet = DynRegNetworking.makeRoundFinishedPacket(Collections.emptyList(), ADDED_ENTRIES
                    .values()
                    .stream()
                    .map(EntryData::entry)
                    .flatMap(x -> Optional.ofNullable(x.toSynced(handler.player)).stream())
                    .toList());

                sender.sendPacket(packet);
            }
        });
    }

    public static void addEntry(EntryData data) {
        ADDED_ENTRIES.put(data.entry().id(), data);
    }

    public static void removeEntry(Identifier id) {
        ADDED_ENTRIES.remove(id);
    }

    public static Map<Identifier, EntryData> getEntries() {
        return ADDED_ENTRIES;
    }
}
