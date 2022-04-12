package me.basiqueevangelist.dynreg.round;

import com.google.common.collect.Lists;
import me.basiqueevangelist.dynreg.client.DynRegClient;
import me.basiqueevangelist.dynreg.holder.LoadedEntryHolder;
import me.basiqueevangelist.dynreg.network.DynRegNetworking;
import me.basiqueevangelist.dynreg.entry.EntryDescription;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
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

    private final List<DynamicRoundTask> tasks = new ArrayList<>();
    private final CompletableFuture<Void> roundEnd = new CompletableFuture<>();
    private final MinecraftServer server;
    private boolean isScheduled = false;
    private final List<RegistryKey<?>> removedEntries = new ArrayList<>();
    private final Map<RegistryKey<?>, EntryDescription<?>> addedEntries = new LinkedHashMap<>();

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

    public void addTask(DynamicRoundTask task) {
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

        for (Registry<?> registry : Registry.REGISTRIES) {
            RegistryUtils.unfreeze(registry);
        }

        var ctx = new Context();
        for (var task : tasks) {
            try {
                task.perform(ctx);
            } catch (Exception e) {
                LOGGER.error("Failed to run task", e);
            }
        }

        for (Registry<?> registry : Registry.REGISTRIES) {
            registry.freeze();
        }

        if (removedEntries.size() > 0)
            LOGGER.info("- Removed {} entries", removedEntries.size());

        if (addedEntries.size() > 0)
            LOGGER.info("- Added {} entries", addedEntries.size());

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            for (var entry : removedEntries)
                DynRegClient.removeRegisteredKey(entry.method_41185(), entry.getValue());

            for (var entry : addedEntries.entrySet())
                DynRegClient.addRegisteredKey(entry.getKey().method_41185(), entry.getKey().getValue());
        }

        for (var entry : removedEntries)
            LoadedEntryHolder.removeRegisteredKey(entry);

        for (var entry : addedEntries.entrySet())
            LoadedEntryHolder.addRegisteredKey(entry.getKey(), entry.getValue());

        if (server == null) {
            LOGGER.info("Finished dynamic round after {} seconds", (System.nanoTime() - time) / 1000000000D);
            return;
        }

        var dataPacket = DynRegNetworking.makeRoundFinishedPacket(removedEntries, addedEntries);
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

    private class Context implements RoundContext {
        @Override
        public <T> T register(Identifier id, EntryDescription<T> desc) {
            addedEntries.put(RegistryKey.of(desc.registry().getKey(), id), desc);
            return Registry.register(desc.registry(), id, desc.create());
        }

        @Override
        public void removeEntry(RegistryKey<?> key) {
            RegistryUtils.remove(key);

            var existingEntry = addedEntries.get(key);

            if (existingEntry != null) {
                addedEntries.remove(key);
            } else {
                removedEntries.add(key);
            }
        }

        @Override
        public void removeEntry(Registry<?> registry, Identifier id) {
            RegistryUtils.remove(registry, id);

            var key = RegistryKey.of(registry.getKey(), id);
            var existingEntry = addedEntries.get(key);

            if (existingEntry != null) {
                addedEntries.remove(key);
            } else {
                removedEntries.add(RegistryKey.of(registry.getKey(), id));
            }
        }
    }
}
