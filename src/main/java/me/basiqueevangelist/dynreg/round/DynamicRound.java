package me.basiqueevangelist.dynreg.round;

import com.google.common.collect.Lists;
import me.basiqueevangelist.dynreg.network.DynRegNetworking;
import me.basiqueevangelist.dynreg.network.block.EntryDescription;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
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

public class DynamicRound {
    private static @Nullable DynamicRound currentRound;
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/DynamicRound");

    private final List<DynamicRoundTask> tasks = new ArrayList<>();
    private final MinecraftServer server;
    private boolean isScheduled = false;
    private final List<RegistryKey<?>> removedEntries = new ArrayList<>();
    private final Map<Identifier, EntryDescription> addedEntries = new HashMap<>();

    public DynamicRound(MinecraftServer server) {

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

    public void run() {
        if (isScheduled) return;
        isScheduled = true;

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
            task.perform(ctx);
        }

        for (Registry<?> registry : Registry.REGISTRIES) {
            registry.freeze();
        }

        LOGGER.info("- Removed {} entries", removedEntries.size());
        LOGGER.info("- Added {} entries", addedEntries.size());

        var dataPacket = DynRegNetworking.makeRoundFinishedPacket(removedEntries, addedEntries);
        var resourcesPacket = DynRegNetworking.makeResourceReloadPacket();

        for (ServerPlayerEntity player : server.getOverworld().getPlayers()) {
            if (!server.isHost(player.getGameProfile()))
                player.networkHandler.sendPacket(dataPacket);
            RegistrySyncManager.sendPacket(server, player);
            player.networkHandler.sendPacket(resourcesPacket);
        }

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
        });
        reloadFuture.exceptionally(e -> {
            LOGGER.warn("Failed to reload resources after round", e);
            return null;
        });
    }

    private class Context implements RoundContext {
        @Override
        public <T> T register(Identifier id, EntryDescription<T> desc) {
            addedEntries.put(id, desc);
            return Registry.register(desc.registry(), id, desc.create());
        }

        @Override
        public void removeEntry(Registry<?> registry, Identifier id) {
            RegistryUtils.remove(registry, id);
            removedEntries.add(RegistryKey.of(registry.getKey(), id));
        }
    }
}
