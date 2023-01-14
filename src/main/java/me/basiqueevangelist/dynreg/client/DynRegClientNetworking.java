package me.basiqueevangelist.dynreg.client;

import me.basiqueevangelist.dynreg.holder.LoadedEntryHolder;
import me.basiqueevangelist.dynreg.network.DynRegNetworking;
import me.basiqueevangelist.dynreg.entry.RegistrationEntries;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.round.DynamicRound;
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
        ClientPlayNetworking.registerGlobalReceiver(DynRegNetworking.ROUND_FINISHED, (client, handler, buf, responseSender) -> {
            try {
                RegistrySyncManager.unmap();
            } catch (RemapException e) {
                LOGGER.error("Failed to unmap registries", e);
            }

            int serverHash = buf.readInt();
            int clientHash = LoadedEntryHolder.hash();

            if (serverHash == clientHash) {
                LOGGER.info("Hashes match, not applying dynamic round");
                return;
            }

            LOGGER.info("Applying dynamic round on client");

            var round = new DynamicRound(client);

            if (!buf.readBoolean())
                round.noResourcePackReload();

            var removedEntriesCount = buf.readVarInt();

            if (removedEntriesCount == -1) {
                for (var entryId : LoadedEntryHolder.entries().keySet()) {
                    round.removeEntry(entryId);
                }
            } else {
                for (int i = 0; i < removedEntriesCount; i++) {
                    Identifier entryId = buf.readIdentifier();

                    round.removeEntry(entryId);
                }
            }

            var addedEntriesCount = buf.readVarInt();

            for (int i = 0; i < addedEntriesCount; i++) {
                Identifier typeId = buf.readIdentifier();
                Identifier entryId = buf.readIdentifier();
                try {
                    RegistrationEntry entry = RegistrationEntries.getEntryDeserializer(typeId).apply(entryId, buf);

                    round.addEntry(entry);
                } catch (Exception e) {
                    LOGGER.error("Encountered error while loading {}", entryId, e);
                }
            }

            if (round.hash() != LoadedEntryHolder.hash())
                round.run();
        });
    }
}
