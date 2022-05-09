package me.basiqueevangelist.dynreg.client;

import me.basiqueevangelist.dynreg.network.DynRegNetworking;
import me.basiqueevangelist.dynreg.entry.RegistrationEntries;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.round.RoundInternals;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynRegClientNetworking {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/ClientNetworking");
    private static long ROUND_RECEIVE_TIME = 0;

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(DynRegNetworking.START_TIMER, ((client, handler, buf, responseSender) -> {
            LOGGER.info("Applying dynamic round on client");
            ROUND_RECEIVE_TIME = System.nanoTime();
        }));

        ClientPlayNetworking.registerGlobalReceiver(DynRegNetworking.RELOAD_RESOURCES, (client, handler, buf, responseSender) -> {
            var future = client.reloadResources();
            future.thenAccept(unused -> {
                LOGGER.info("Applied dynamic round after {} seconds", (System.nanoTime() - ROUND_RECEIVE_TIME) / 1000000000D);
            });
            future.exceptionally(e -> {
                LOGGER.warn("Failed to reload resources after round", e);
                return null;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(DynRegNetworking.ROUND_FINISHED, (client, handler, buf, responseSender) -> {
            LOGGER.info("Applying dynamic round on client");
            ROUND_RECEIVE_TIME = System.nanoTime();

            for (Registry<?> registry : Registry.REGISTRIES) {
                RegistryUtils.unfreeze(registry);
            }

            var removedEntriesCount = buf.readVarInt();

            for (int i = 0; i < removedEntriesCount; i++) {
                Identifier entryId = buf.readIdentifier();

                RoundInternals.removeEntry(entryId);
            }

            var addedEntriesCount = buf.readVarInt();

            for (int i = 0; i < addedEntriesCount; i++) {
                Identifier typeId = buf.readIdentifier();
                Identifier entryid = buf.readIdentifier();
                RegistrationEntry entry = RegistrationEntries.getEntryDeserializer(typeId).apply(entryid, buf);

                RoundInternals.registerEntry(entry);
            }

            for (Registry<?> registry : Registry.REGISTRIES) {
                registry.freeze();
            }
        });
    }
}
