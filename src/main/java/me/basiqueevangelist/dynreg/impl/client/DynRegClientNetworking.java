package me.basiqueevangelist.dynreg.impl.client;

import me.basiqueevangelist.dynreg.api.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.impl.DynRegNetworking;
import me.basiqueevangelist.dynreg.impl.entry.RegistrationEntriesImpl;
import me.basiqueevangelist.dynreg.impl.holder.LoadedEntryHolder;
import me.basiqueevangelist.dynreg.impl.round.ModificationRoundImpl;
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

            long serverHash = buf.readLong();
            long clientHash = LoadedEntryHolder.hash();

            if (serverHash == clientHash) {
                LOGGER.info("Hashes match, not applying dynamic round");
                return;
            }

            LOGGER.info("Applying dynamic round on client");

            var round = new ModificationRoundImpl(client);

            if (buf.readBoolean())
                round.reloadResourcePacks();

            for (var entryId : LoadedEntryHolder.entries().keySet()) {
                round.removeEntry(entryId);
            }

            var addedEntriesCount = buf.readVarInt();

            for (int i = 0; i < addedEntriesCount; i++) {
                Identifier typeId = buf.readIdentifier();
                Identifier entryId = buf.readIdentifier();
                try {
                    RegistrationEntry entry = RegistrationEntriesImpl.getNetworkData(typeId).deserializer().apply(entryId, buf);

                    round.entry(entry);
                } catch (Exception e) {
                    LOGGER.error("Encountered error while loading {}", entryId, e);
                }
            }

            round.run();
        });
    }
}
