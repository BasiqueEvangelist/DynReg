package me.basiqueevangelist.dynreg.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Invoked before Quilt and Fabric registry sync
 */
public interface PreSyncCallback {

    Event<PreSyncCallback> EVENT = EventFactory.createWithPhases(PreSyncCallback.class,
        callbacks -> (server, player, connection) -> {
            for (var cb : callbacks) {
                cb.onPreSync(server, player, connection);
            }
        }, Event.DEFAULT_PHASE);

    void onPreSync(MinecraftServer server, ServerPlayerEntity player, ClientConnection connection);
}
