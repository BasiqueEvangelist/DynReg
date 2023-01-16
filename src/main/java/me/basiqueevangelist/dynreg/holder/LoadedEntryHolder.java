package me.basiqueevangelist.dynreg.holder;

import me.basiqueevangelist.dynreg.DynReg;
import me.basiqueevangelist.dynreg.client.DynRegClient;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.network.DynRegNetworking;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

            List<RegistrationEntry> syncedEntries = new ArrayList<>();
            EntryHasher hasher = new EntryHasher();

            for (var entry : LoadedEntryHolder.entries().values()) {
                var synced = entry.entry().toSynced(handler.player);

                if (synced != null) {
                    syncedEntries.add(synced);
                    hasher.accept(synced);
                }
            }

            Packet<?> packet = DynRegNetworking.makeRoundFinishedPacket(hasher.hash(), doResourceReload, syncedEntries);
            handler.sendPacket(packet);
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

    public static long hash() {
        var hasher = new EntryHasher();

        ADDED_ENTRIES.values().forEach(x -> hasher.accept(x.entry()));

        return hasher.hash();
    }

    public static Map<Identifier, EntryData> entries() {
        return ADDED_ENTRIES;
    }

    public static Map<Identifier, EntryData> startupEntries() {
        return STARTUP_ENTRIES;
    }
}
