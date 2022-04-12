package me.basiqueevangelist.dynreg.holder;

import me.basiqueevangelist.dynreg.network.DynRegNetworking;
import me.basiqueevangelist.dynreg.entry.EntryDescription;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.network.Packet;
import net.minecraft.util.registry.RegistryKey;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LoadedEntryHolder {
    private static final Map<RegistryKey<?>, EntryDescription<?>> ADDED_ENTRIES = new LinkedHashMap<>();

    private LoadedEntryHolder() {

    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ADDED_ENTRIES.clear();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (ADDED_ENTRIES.size() == 0) return;

            boolean isHost = server.isHost(handler.player.getGameProfile());

            if (!isHost) {
                Packet<?> packet = DynRegNetworking.makeRoundFinishedPacket(Collections.emptyList(), ADDED_ENTRIES);

                sender.sendPacket(packet);
            } else {
                sender.sendPacket(DynRegNetworking.START_TIMER_PACKET);
            }

            RegistrySyncManager.sendPacket(server, handler.player);
            sender.sendPacket(DynRegNetworking.RELOAD_RESOURCES_PACKET);
        });
    }

    public static void addRegisteredKey(RegistryKey<?> key, EntryDescription<?> value) {
        ADDED_ENTRIES.put(key, value);
    }

    public static void removeRegisteredKey(RegistryKey<?> key) {
        ADDED_ENTRIES.remove(key);
    }
}
