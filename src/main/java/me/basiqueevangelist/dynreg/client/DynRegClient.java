package me.basiqueevangelist.dynreg.client;

import me.basiqueevangelist.dynreg.client.event.PostLeaveRoundCallback;
import me.basiqueevangelist.dynreg.client.fixer.ClientBlockFixer;
import me.basiqueevangelist.dynreg.client.fixer.ClientEntityFixer;
import me.basiqueevangelist.dynreg.client.fixer.ClientItemFixer;
import me.basiqueevangelist.dynreg.holder.LoadedEntryHolder;
import me.basiqueevangelist.dynreg.round.DynamicRound;
import me.basiqueevangelist.dynreg.util.ExecutorFreezer;
import me.basiqueevangelist.dynreg.util.InfallibleCloseable;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DynRegClient implements ClientModInitializer {
    private static long CURRENT_RESOURCE_HASH = 0;

    @Override
    public void onInitializeClient() {
        ClientBlockFixer.init();
        ClientItemFixer.init();
        ClientEntityFixer.init();
        DynRegClientNetworking.init();

        PostLeaveRoundCallback.EVENT.register(round -> {
            for (var entryId : LoadedEntryHolder.entries().keySet())
                round.removeEntry(entryId);

            for (var entry : LoadedEntryHolder.startupEntries().values())
                round.addEntry(entry.entry());
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            var round = new DynamicRound(client);
            PostLeaveRoundCallback.EVENT.invoker().onClientDisconnect(round);

            if (round.needsRunning())
                round.run();
        });
    }

    public static void markReload() {
        CURRENT_RESOURCE_HASH = LoadedEntryHolder.hash();
    }

    public static InfallibleCloseable freezeClientThread() {
        try {
            return ExecutorFreezer.freeze(MinecraftClient.getInstance()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static CompletableFuture<Void> reloadClientResources() {
        if (CURRENT_RESOURCE_HASH == LoadedEntryHolder.hash())
            return CompletableFuture.completedFuture(null);

        return MinecraftClient.getInstance().reloadResources();
    }
}
