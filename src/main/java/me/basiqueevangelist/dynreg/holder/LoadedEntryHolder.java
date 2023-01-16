package me.basiqueevangelist.dynreg.holder;

import me.basiqueevangelist.dynreg.DynReg;
import me.basiqueevangelist.dynreg.client.DynRegClient;
import me.basiqueevangelist.dynreg.network.DynRegNetworking;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;

import java.util.*;

public final class LoadedEntryHolder {
    private static final Map<Identifier, EntryData> ADDED_ENTRIES = new LinkedHashMap<>();
    private static final Map<Identifier, EntryData> STARTUP_ENTRIES = new LinkedHashMap<>();
    private static final Identifier ROUND_SYNC_PHASE = DynReg.id("round_sync");

    private LoadedEntryHolder() {

    }

    public static void init() {
        ServerPlayConnectionEvents.JOIN.addPhaseOrdering(ROUND_SYNC_PHASE, Event.DEFAULT_PHASE);
        ServerPlayConnectionEvents.JOIN.register(ROUND_SYNC_PHASE, (handler, sender, server) -> {
            if (ADDED_ENTRIES.size() == 0) return;

            boolean doResourceReload = server.getResourcePackProperties().isEmpty();

            boolean isHost = server.isHost(handler.player.getGameProfile());

            if (isHost) {
                if (doResourceReload)
                    DynRegClient.reloadClientResources();

                return;
            }

            // FIX: make this less stream-y.
            Packet<?> packet = DynRegNetworking.makeRoundFinishedPacket(hash(), doResourceReload,
                null,
                ADDED_ENTRIES
                    .values()
                    .stream()
                    .map(EntryData::entry)
                    .flatMap(x -> Optional.ofNullable(x.toSynced(handler.player)).stream())
                    .toList());

            sender.sendPacket(packet);
        });
    }

    public static void addEntry(EntryData data) {
        ADDED_ENTRIES.put(data.entry().id(), data);

        if (data.isStartup())
            STARTUP_ENTRIES.put(data.entry().id(), data);
    }

    public static void removeEntry(Identifier id) {
        ADDED_ENTRIES.remove(id);
    }

    public static int hash() {
        TreeMap<Identifier, EntryData> entries = new TreeMap<>(ADDED_ENTRIES);
        int hash = 0;

        for (var entry : entries.entrySet()) {
            hash = 31 * hash + entry.getKey().hashCode();
            hash = 31 * hash + entry.getValue().entry().hash();
        }

        return hash;
    }

    public static int hashStartup() {
        TreeMap<Identifier, EntryData> entries = new TreeMap<>(STARTUP_ENTRIES);
        int hash = 0;

        for (var entry : entries.entrySet()) {
            hash = 31 * hash + entry.getKey().hashCode();
            hash = 31 * hash + entry.getValue().entry().hash();
        }

        return hash;
    }

    public static Map<Identifier, EntryData> entries() {
        return ADDED_ENTRIES;
    }

    public static Map<Identifier, EntryData> startupEntries() {
        return STARTUP_ENTRIES;
    }
}
