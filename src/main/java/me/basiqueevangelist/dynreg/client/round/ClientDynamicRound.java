package me.basiqueevangelist.dynreg.client.round;

import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClientDynamicRound {
    private static @Nullable ClientDynamicRound currentRound;
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/ClientDynamicRound");

    private final List<Runnable> tasks = new ArrayList<>();
    private final CompletableFuture<Void> roundEnd = new CompletableFuture<>();
    private final MinecraftClient client = MinecraftClient.getInstance();
    private boolean isScheduled = false;

    private ClientDynamicRound() {
    }

    public static ClientDynamicRound getRound() {
        if (currentRound == null) {
            currentRound = new ClientDynamicRound();
        }

        return currentRound;
    }

    public void addTask(Runnable task) {
        tasks.add(task);
    }

    public CompletableFuture<Void> getRoundEndFuture() {
        return roundEnd;
    }

    public void run() {
        if (isScheduled) return;
        if (tasks.size() == 0) return;

        isScheduled = true;

        client.execute(this::doRun);
    }

    private void doRun() {
        if (tasks.size() == 0) {
            roundEnd.complete(null);
            return;
        }

        LOGGER.info("Starting client dynamic round");
        long time = System.nanoTime();
        currentRound = null;

        for (Registry<?> registry : Registry.REGISTRIES) {
            RegistryUtils.unfreeze(registry);
        }

        for (var task : tasks) {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.error("Failed to run task", e);
            }
        }

        for (Registry<?> registry : Registry.REGISTRIES) {
            registry.freeze();
        }

        var future = client.reloadResources();
        future.thenAccept(unused -> {
            LOGGER.info("Finished dynamic round after {} seconds", (System.nanoTime() - time) / 1000000000D);
            roundEnd.complete(null);
        });
        future.exceptionally(e -> {
            LOGGER.warn("Failed to reload resources after round", e);
            roundEnd.completeExceptionally(e);
            return null;
        });
    }
}
