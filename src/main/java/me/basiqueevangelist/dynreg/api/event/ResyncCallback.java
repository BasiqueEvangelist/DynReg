package me.basiqueevangelist.dynreg.api.event;

import me.basiqueevangelist.dynreg.DynReg;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public interface ResyncCallback {
    Identifier SYNC_ENTRIES = DynReg.id("send_all_entries");
    Identifier REGISTRY_SYNC = DynReg.id("registry_sync");

    Event<ResyncCallback> EVENT = EventFactory.createWithPhases(ResyncCallback.class,
        callbacks -> (server, player, reloadResourcePacks) -> {
            for (var cb : callbacks) {
                cb.onResync(server, player, reloadResourcePacks);
            }
        }, SYNC_ENTRIES, REGISTRY_SYNC, Event.DEFAULT_PHASE);

    /**
     * Invoked whenever registry changes are to be synced to the player.
     * @param server the server
     * @param player the player to sync to
     * @param reloadResourcePacks whether resource packs should be reloaded on the client
     */
    void onResync(MinecraftServer server, ServerPlayerEntity player, boolean reloadResourcePacks);
}
