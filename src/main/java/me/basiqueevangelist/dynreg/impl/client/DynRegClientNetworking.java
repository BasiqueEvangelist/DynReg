package me.basiqueevangelist.dynreg.impl.client;

import me.basiqueevangelist.dynreg.api.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.impl.DynRegNetworking;
import me.basiqueevangelist.dynreg.impl.RoundFinishedS2CPacket;
import me.basiqueevangelist.dynreg.impl.entry.RegistrationEntriesImpl;
import me.basiqueevangelist.dynreg.impl.holder.LoadedEntryHolder;
import me.basiqueevangelist.dynreg.impl.round.ModificationRoundImpl;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.fabric.impl.registry.sync.RemapException;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynRegClientNetworking {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/ClientNetworking");

    @SuppressWarnings("UnstableApiUsage")
    public static void init() {
        ClientConfigurationNetworking.registerGlobalReceiver(RoundFinishedS2CPacket.ID, (packet, ctx) -> {
            try {
                RegistrySyncManager.unmap();
            } catch (RemapException e) {
                LOGGER.error("Failed to unmap registries", e);
            }

            long serverHash = packet.hash();
            long clientHash = LoadedEntryHolder.hash();

            if (serverHash == clientHash) {
                LOGGER.info("Hashes match, not applying dynamic round");
                return;
            }

            LOGGER.info("Applying dynamic round on client");

            var round = new ModificationRoundImpl(ctx.client());

            if (packet.reloadResources())
                round.reloadResourcePacks();

            for (var entryId : LoadedEntryHolder.entries().keySet()) {
                round.removeEntry(entryId);
            }

            for (var entry : packet.addedEntries()) {
                round.entry(entry);
            }

            round.run();
        });
    }
}
