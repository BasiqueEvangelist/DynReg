package me.basiqueevangelist.dynreg.round;

import com.google.common.collect.Lists;
import me.basiqueevangelist.dynreg.client.DynRegClient;
import me.basiqueevangelist.dynreg.holder.EntryData;
import me.basiqueevangelist.dynreg.holder.LoadedEntryHolder;
import me.basiqueevangelist.dynreg.network.DynRegNetworking;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.TopologicalSorts;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.SaveProperties;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DynamicRound {
    private static @Nullable DynamicRound currentRound;
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/DynamicRound");

    private final List<Identifier> removedEntries = new ArrayList<>();
    private final Map<Identifier, RegistrationEntry> addedEntries = new HashMap<>();
    private final CompletableFuture<Void> roundEnd = new CompletableFuture<>();
    private final MinecraftServer server;
    private boolean isScheduled = false;

    private boolean reloadDataPacks = true;
    private boolean reloadResourcePacks = true;

    private DynamicRound(MinecraftServer server) {
        this.server = server;
    }

    public static DynamicRound getRound(MinecraftServer server) {
        if (currentRound == null) {
            currentRound = new DynamicRound(server);
        }

        return currentRound;
    }

    public void removeEntry(Identifier id) {
        removedEntries.add(id);
    }

    public void addEntry(RegistrationEntry entry) {
        addedEntries.put(entry.id(), entry);
    }

    public void noDataPackReload() {
        reloadDataPacks = false;
    }

    public void noResourcePackReload() {
        reloadResourcePacks = false;
    }

    public CompletableFuture<Void> getRoundEndFuture() {
        return roundEnd;
    }

    public void run() {
        if (isScheduled) return;
        isScheduled = true;

        if (server == null)
            doRun();
        else
            server.execute(this::doRun);
    }

    private void doRun() {
        LOGGER.info("Starting dynamic round");
        long time = System.nanoTime();
        currentRound = null;

        var idToEntry = new HashMap<Identifier, RegistrationEntry>();

        for (var entry : addedEntries.values()) {
            idToEntry.put(entry.id(), entry);
        }

        for (Registry<?> registry : Registry.REGISTRIES) {
            RegistryUtils.unfreeze(registry);
        }

        for (Identifier oldEntryId : removedEntries) {
            RoundInternals.removeEntry(oldEntryId);
        }

        EntryScanner scanner = new EntryScanner(addedEntries.values());

        var deps = scanner.scan();
        var visited = new HashSet<Identifier>();
        var visiting = new HashSet<Identifier>();
        var order = new ArrayList<Identifier>();

        for (var entry : addedEntries.values()) {
            if (!visited.contains(entry.id()) && TopologicalSorts.sort(deps, visited, visiting, order::add, entry.id())) {
                LOGGER.warn("Entry order cycle!");
            }

        }

        Collections.reverse(order);

        for (var entryId : order) {
            RoundInternals.registerEntry(idToEntry.get(entryId));
        }

        for (Registry<?> registry : Registry.REGISTRIES) {
            registry.freeze();
        }

        if (server == null) {
            LOGGER.info("Finished dynamic round after {} seconds", (System.nanoTime() - time) / 1000000000D);
            return;
        }

        var dataPacket = DynRegNetworking.makeRoundFinishedPacket(removedEntries, addedEntries.values());
        for (ServerPlayerEntity player : server.getOverworld().getPlayers()) {
            if (!server.isHost(player.getGameProfile()) && (addedEntries.size() > 0 || removedEntries.size() > 0))
                player.networkHandler.sendPacket(dataPacket);
            else
                player.networkHandler.sendPacket(DynRegNetworking.START_TIMER_PACKET);

            RegistrySyncManager.sendPacket(server, player);

            if (reloadResourcePacks)
                player.networkHandler.sendPacket(DynRegNetworking.RELOAD_RESOURCES_PACKET);
            else
                player.networkHandler.sendPacket(DynRegNetworking.STOP_TIMER_PACKET);
        }

        if (reloadDataPacks) {
            ResourcePackManager resourcePackManager = server.getDataPackManager();
            SaveProperties saveProperties = server.getSaveProperties();
            Collection<String> enabledDataPacks = resourcePackManager.getEnabledNames();
            resourcePackManager.scanPacks();
            Collection<String> dataPacks = Lists.newArrayList(enabledDataPacks);
            Collection<String> disabledDataPacks = saveProperties.getDataPackSettings().getDisabled();

            for(String string : resourcePackManager.getNames()) {
                if (!disabledDataPacks.contains(string) && !dataPacks.contains(string)) {
                    dataPacks.add(string);
                }
            }

            var reloadFuture = server.reloadResources(dataPacks);
            reloadFuture.thenAccept(unused -> {
                LOGGER.info("Finished dynamic round after {} seconds", (System.nanoTime() - time) / 1000000000D);
                roundEnd.complete(null);
            });
            reloadFuture.exceptionally(e -> {
                LOGGER.warn("Failed to reload resources after round", e);
                roundEnd.completeExceptionally(e);
                return null;
            });
        } else {
            LOGGER.info("Finished dynamic round after {} seconds", (System.nanoTime() - time) / 1000000000D);
        }
    }
}
