package me.basiqueevangelist.dynreg.round;

import com.google.common.collect.Lists;
import me.basiqueevangelist.dynreg.client.DynRegClient;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.event.ResyncCallback;
import me.basiqueevangelist.dynreg.event.RoundEvents;
import me.basiqueevangelist.dynreg.holder.EntryData;
import me.basiqueevangelist.dynreg.holder.EntryHasher;
import me.basiqueevangelist.dynreg.holder.LoadedEntryHolder;
import me.basiqueevangelist.dynreg.util.InfallibleCloseable;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import me.basiqueevangelist.dynreg.util.TopSort;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
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

    private boolean reloadDataPacks = false;
    private boolean reloadResourcePacks = true;
    private boolean startupEntries = false;

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

    public void dataPackReload() {
        if (server == null)
            throw new IllegalStateException("cannot force datapack on client");

        reloadDataPacks = true;
    }

    public void noResourcePackReload() {
        reloadResourcePacks = false;
    }

    public void markAsStartup() {
        startupEntries = true;
    }

    public CompletableFuture<Void> getRoundEndFuture() {
        return roundEnd;
    }

    public long hash() {
        var removedEntries = new ArrayList<EntryData>();

        for (Identifier oldEntryId : removedEntryIds) {
            var entry = LoadedEntryHolder.entries().get(oldEntryId);

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

        EntryHasher hasher = new EntryHasher();

        for (var oldEntry : LoadedEntryHolder.entries().entrySet()) {
            hasher.accept(oldEntry.getValue().entry());
        }

        for (var removed : removedEntries) {
            hasher.remove(removed.entry());
        }

        for (var added : addedEntries.values()) {
            hasher.accept(added);
        }

        return hasher.hash();
    }

    public boolean needsRunning() {
        return tasks.size() > 0 || hash() != LoadedEntryHolder.hash();
    }

    public void run() {
        try {
            InfallibleCloseable clientUnfreezer =
                FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && server != null
                    ? DynRegClient.freezeClientThread()
                    : () -> {};

            LOGGER.info("Starting dynamic round");
            long time = System.nanoTime();

            try (clientUnfreezer) {
                for (Registry<?> registry : Registries.REGISTRIES) {
                    RegistryUtils.unfreeze(registry);
                }

                RoundEvents.PRE.invoker().preRound();

                var removedEntries = new ArrayList<EntryData>();

                for (Identifier oldEntryId : removedEntryIds) {
                    var entry = LoadedEntryHolder.entries().get(oldEntryId);

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

                    removedEntry.entry().onRemoved();

                    for (RegistryKey<?> registeredKey : removedEntry.registeredKeys()) {
                        try {
                            RegistryUtils.remove(registeredKey);
                        } catch (NoSuchElementException e) {
                            LOGGER.error("{} is registered, but has removed {}/{}", removedEntry.entry().id(), registeredKey.getRegistry(), registeredKey.getValue());
                        }
                    }

                    LoadedEntryHolder.removeEntry(removedEntry.entry().id());
                }

                EntryScanner scanner = new EntryScanner(addedEntries.values(), startupEntries);

                var entries = scanner.scan();
                var cycle = new MutableBoolean(false);
                var order = TopSort.topSort(entries.values(), EntryData::dependents, cycle);

                for (var entry : order) {
                    try {
                        entry.entry().register(entry.createRegistrationContext());

                        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
                            entry.entry().registerClient();

                        LoadedEntryHolder.addEntry(entry);
                    } catch (Exception e) {
                        LOGGER.error("Encountered error while registering {}", entry.entry().id(), e);
                    }
                }

                for (var task : tasks) {
                    task.run();
                }

                for (Registry<?> registry : Registries.REGISTRIES) {
                    registry.freeze();
                }

                RoundEvents.POST.invoker().postRound();

            }

            CompletableFuture<Void> reloadFuture = null;

            if (server != null) {
                for (ServerPlayerEntity player : server.getOverworld().getPlayers()) {
                    ResyncCallback.EVENT.invoker().onResync(server, player, reloadResourcePacks);
                }

                if (reloadDataPacks) {
                    ResourcePackManager resourcePackManager = server.getDataPackManager();
                    SaveProperties saveProperties = server.getSaveProperties();
                    Collection<String> enabledDataPacks = resourcePackManager.getEnabledNames();
                    resourcePackManager.scanPacks();
                    Collection<String> dataPacks = Lists.newArrayList(enabledDataPacks);
                    Collection<String> disabledDataPacks = saveProperties.getDataConfiguration().dataPacks().getDisabled();

                    for (String string : resourcePackManager.getNames()) {
                        if (!disabledDataPacks.contains(string) && !dataPacks.contains(string)) {
                            dataPacks.add(string);
                        }
                    }

                    reloadFuture = server.reloadResources(dataPacks);

                }
            }

            if (client != null && reloadResourcePacks) {
                reloadFuture = DynRegClient.reloadClientResources();
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
