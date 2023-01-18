package me.basiqueevangelist.dynreg.fixer;

import me.basiqueevangelist.dynreg.access.DeletableObjectInternal;
import me.basiqueevangelist.dynreg.client.DynRegClient;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.event.ResyncCallback;
import me.basiqueevangelist.dynreg.holder.EntryHasher;
import me.basiqueevangelist.dynreg.holder.LoadedEntryHolder;
import me.basiqueevangelist.dynreg.network.DynRegNetworking;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.network.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class GlobalFixer<T> {
    private final Registry<T> registry;

    private GlobalFixer(Registry<T> registry) {
        this.registry = registry;
    }

    public static void init() {
        for (Registry<?> registry : Registries.REGISTRIES) {
            new GlobalFixer<>(registry).register();
        }

        ResyncCallback.EVENT.register(ResyncCallback.SYNC_ENTRIES, GlobalFixer::syncEntries);
        ResyncCallback.EVENT.register(ResyncCallback.REGISTRY_SYNC, GlobalFixer::registrySync);
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
            var synced = entry.entry().toSynced(player);

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
        RegistrySyncManager.sendPacket(server, player);
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
}
