package me.basiqueevangelist.dynreg.round;

import com.google.common.collect.Lists;
import me.basiqueevangelist.dynreg.client.DynRegClient;
import me.basiqueevangelist.dynreg.holder.EntryData;
import me.basiqueevangelist.dynreg.holder.LoadedEntryHolder;
import me.basiqueevangelist.dynreg.network.DynRegNetworking;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import me.basiqueevangelist.dynreg.util.TopSort;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.SaveProperties;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DynamicRound {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/DynamicRound");

    private final List<Identifier> removedEntryIds = new ArrayList<>();
    private final Map<Identifier, RegistrationEntry> addedEntries = new HashMap<>();
    private final CompletableFuture<Void> roundEnd = new CompletableFuture<>();
    private final List<Runnable> tasks = new ArrayList<>();
    private final MinecraftServer server;
    private final /* MinecraftClient */ Object client;

    private boolean reloadDataPacks = true;
    private boolean reloadResourcePacks = true;

    public DynamicRound(MinecraftServer server) {
        this.server = server;
        this.client = null;
    }

    @Environment(EnvType.CLIENT)
    public DynamicRound(MinecraftClient client) {
        this.server = null;
        this.client = client;
        this.reloadDataPacks = false;
    }

    public void removeEntry(Identifier id) {
        removedEntryIds.add(id);
    }

    public void addEntry(RegistrationEntry entry) {
        addedEntries.put(entry.id(), entry);
    }

    public void addTask(Runnable task) {
        tasks.add(task);
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
        try {
            LOGGER.info("Starting dynamic round");
            long time = System.nanoTime();

            for (Registry<?> registry : Registry.REGISTRIES) {
                RegistryUtils.unfreeze(registry);
            }

            var removedSyncedEntryIds = new ArrayList<Identifier>();
            var removedEntries = new ArrayList<EntryData>();

            for (Identifier oldEntryId : removedEntryIds) {
                var entry = LoadedEntryHolder.getEntries().get(oldEntryId);

                if (entry == null) {
                    LOGGER.warn("Removed entry {} was already removed", oldEntryId);
                    continue;
                }

                removedEntries.add(entry);
            }

            for (int i = 0; i < removedEntries.size(); i++) {
                for (var dependent : removedEntries.get(i).dependents()) {
                    if (!removedEntries.contains(dependent))
                        removedEntries.add(dependent);
                }
            }

            for (var removedEntry : removedEntries) {
                for (var dependency : removedEntry.dependencies())
                    dependency.dependents().remove(removedEntry);

                if (removedEntry.entry().isSynced())
                    removedSyncedEntryIds.add(removedEntry.entry().id());

                removedEntry.entry().onRemoved();

                for (RegistryKey<?> registeredKey : removedEntry.registeredKeys()) {
                    RegistryUtils.remove(registeredKey);
                }

                LoadedEntryHolder.removeEntry(removedEntry.entry().id());
            }

            EntryScanner scanner = new EntryScanner(addedEntries.values());

            var entries = scanner.scan();
            var cycle = new MutableBoolean(false);
            var order = TopSort.topSort(entries.values(), EntryData::dependents, cycle);
            var addedSyncedEntries = new ArrayList<RegistrationEntry>();

            for (var entry : order) {
                try {
                    if (entry.entry().isSynced())
                        addedSyncedEntries.add(entry.entry());

                    entry.entry().register(entry.createRegistrationContext());

                    LoadedEntryHolder.addEntry(entry);
                } catch (Exception e) {
                    LOGGER.error("Encountered error while registering {}", entry.entry().id(), e);
                }
            }

            for (var task : tasks) {
                task.run();
            }

            for (Registry<?> registry : Registry.REGISTRIES) {
                registry.freeze();
            }

            CompletableFuture<Void> reloadFuture = null;

            if (server != null) {
                var dataPacket = DynRegNetworking.makeRoundFinishedPacket(removedSyncedEntryIds, addedSyncedEntries);
                for (ServerPlayerEntity player : server.getOverworld().getPlayers()) {
                    if (!server.isHost(player.getGameProfile()) && (addedEntries.size() > 0 || removedEntryIds.size() > 0))
                        player.networkHandler.sendPacket(dataPacket);
                    else
                        player.networkHandler.sendPacket(DynRegNetworking.RELOAD_RESOURCES_PACKET);

                    RegistrySyncManager.sendPacket(server, player);

                }

                if (reloadDataPacks) {
                    ResourcePackManager resourcePackManager = server.getDataPackManager();
                    SaveProperties saveProperties = server.getSaveProperties();
                    Collection<String> enabledDataPacks = resourcePackManager.getEnabledNames();
                    resourcePackManager.scanPacks();
                    Collection<String> dataPacks = Lists.newArrayList(enabledDataPacks);
                    Collection<String> disabledDataPacks = saveProperties.getDataPackSettings().getDisabled();

                    for (String string : resourcePackManager.getNames()) {
                        if (!disabledDataPacks.contains(string) && !dataPacks.contains(string)) {
                            dataPacks.add(string);
                        }
                    }

                    reloadFuture = server.reloadResources(dataPacks);

                }
            }

            if (client != null && reloadResourcePacks) {
                reloadFuture = DynRegClient.reloadClientResources(client);
            }

            if (reloadFuture != null) {
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
                roundEnd.complete(null);
            }
        } catch (Exception e) {
            LOGGER.error("Encountered error while running dynamic round", e);
            roundEnd.completeExceptionally(e);
            throw e;
        }
    }
}
