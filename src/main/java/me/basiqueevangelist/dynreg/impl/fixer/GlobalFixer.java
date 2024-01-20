package me.basiqueevangelist.dynreg.impl.fixer;

import me.basiqueevangelist.dynreg.api.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.api.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.impl.DynReg;
import me.basiqueevangelist.dynreg.impl.DynRegNetworking;
import me.basiqueevangelist.dynreg.impl.access.DeletableObjectInternal;
import me.basiqueevangelist.dynreg.impl.client.DynRegClient;
import me.basiqueevangelist.dynreg.impl.holder.EntryHasher;
import me.basiqueevangelist.dynreg.impl.holder.LoadedEntryHolder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class GlobalFixer<T> {
    public static final Identifier SYNC_ENTRIES = DynReg.id("send_all_entries");

    private final Registry<T> registry;

    private GlobalFixer(Registry<T> registry) {
        this.registry = registry;
    }

    public static void init() {
        for (Registry<?> registry : Registries.REGISTRIES) {
            new GlobalFixer<>(registry).register();
        }

        ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.addPhaseOrdering(SYNC_ENTRIES, Event.DEFAULT_PHASE);
        ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.register(SYNC_ENTRIES, GlobalFixer::syncEntries);

        ServerConfigurationNetworking.registerGlobalReceiver(DynRegNetworking.ROUND_SYNC_COMPLETE, (server, handler, buf, responseSender) -> {
            handler.completeTask(ResyncEntriesTask.KEY);
        });
    }

    private static void syncEntries(ServerConfigurationNetworkHandler handler, MinecraftServer server) {
        if (server.isHost(handler.getDebugProfile())) {
            // TODO: actually like, check if you need to reload res packs

//            if (reloadResourcePacks)
                DynRegClient.reloadClientResources();

            return;
        }

        if (!ServerConfigurationNetworking.canSend(handler, DynRegNetworking.ROUND_FINISHED))
            return;

        handler.addTask(new ResyncEntriesTask(handler));
    }

    private void register() {
        RegistryEntryAddedCallback.event(registry).register(this::onEntryAdded);
        RegistryEntryDeletedCallback.event(registry).register(this::onEntryDeleted);
    }

    private static void syncEntries(MinecraftServer server, ServerPlayerEntity player, boolean reloadResourcePacks) {
        if (server.isHost(player.getGameProfile())) {
            if (reloadResourcePacks)
                DynRegClient.reloadClientResources();

            return;
        }

        List<RegistrationEntry> syncedEntries = new ArrayList<>();
        EntryHasher hasher = new EntryHasher();

        for (var entry : LoadedEntryHolder.entries().values()) {
            var synced = entry.entry().toSynced(player.networkHandler);

            if (synced != null) {
                syncedEntries.add(synced);
                hasher.accept(synced);
            }
        }

        Packet<?> packet = DynRegNetworking.makeRoundFinishedPacket(hasher.hash(), reloadResourcePacks, syncedEntries);
        player.networkHandler.sendPacket(packet);
    }

    private static void registrySync(MinecraftServer server, ServerPlayerEntity player, boolean reloadResourcePacks) {
        //noinspection UnstableApiUsage
//        RegistrySyncManager.sendPacket(server, player);
    }

    private void onEntryDeleted(int rawId, RegistryEntry.Reference<?> entry) {
        ((DeletableObjectInternal) entry).dynreg$setDeleted(true);

        if (entry.value() instanceof DeletableObjectInternal obj) {
            obj.dynreg$setDeleted(true);
            obj.dynreg$setId(entry.getKey().orElseThrow().getValue());
        }
    }

    private void onEntryAdded(int rawId, Identifier id, Object obj) {
        ((DeletableObjectInternal) registry.getEntry(rawId).orElseThrow()).dynreg$setDeleted(false);

        if (obj instanceof DeletableObjectInternal doi) {
            doi.dynreg$setDeleted(false);
            doi.dynreg$setId(id);
        }
    }

    private record ResyncEntriesTask(ServerConfigurationNetworkHandler handler)
            implements ServerPlayerConfigurationTask {
        public static final Key KEY = new Key(DynReg.id("resync_entries").toString());

        @Override
        public void sendPacket(Consumer<Packet<?>> sender) {
            List<RegistrationEntry> syncedEntries = new ArrayList<>();
            EntryHasher hasher = new EntryHasher();

            for (var entry : LoadedEntryHolder.entries().values()) {
                var synced = entry.entry().toSynced(handler);

                if (synced != null) {
                    syncedEntries.add(synced);
                    hasher.accept(synced);
                }
            }

            // TODO: ACTUALLY CHECK.
            Packet<?> packet = DynRegNetworking.makeRoundFinishedPacket(hasher.hash(), true, syncedEntries);
            sender.accept(packet);
        }

        @Override
        public Key getKey() {
            return KEY;
        }
    }
}
