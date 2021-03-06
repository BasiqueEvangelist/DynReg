package me.basiqueevangelist.dynreg.client;

import me.basiqueevangelist.dynreg.client.event.PostLeaveRoundCallback;
import me.basiqueevangelist.dynreg.client.fixer.ClientBlockFixer;
import me.basiqueevangelist.dynreg.client.fixer.ClientItemFixer;
import me.basiqueevangelist.dynreg.holder.LoadedEntryHolder;
import me.basiqueevangelist.dynreg.round.DynamicRound;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;

import java.util.concurrent.CompletableFuture;

public class DynRegClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientBlockFixer.init();
        ClientItemFixer.init();
        DynRegClientNetworking.init();

        PostLeaveRoundCallback.EVENT.register(round -> {
            if (LoadedEntryHolder.getEntries().size() > 0) {
                for (var entryId : LoadedEntryHolder.getEntries().keySet())
                    round.removeEntry(entryId);
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            var round = new DynamicRound(client);
            PostLeaveRoundCallback.EVENT.invoker().onClientDisconnect(round);
            round.run();
        });
    }

    public static CompletableFuture<Void> reloadClientResources(Object client) {
        return ((MinecraftClient) client).reloadResources();
    }
}
