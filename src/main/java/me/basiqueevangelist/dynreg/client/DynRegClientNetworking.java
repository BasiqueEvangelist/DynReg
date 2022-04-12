package me.basiqueevangelist.dynreg.client;

import me.basiqueevangelist.dynreg.network.DynRegNetworking;
import me.basiqueevangelist.dynreg.entry.EntryDescriptions;
import me.basiqueevangelist.dynreg.entry.EntryDescription;
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

            LOGGER.info(" - Removed {} entries", removedEntriesCount);

            for (int i = 0; i < removedEntriesCount; i++) {
                Identifier registryId = buf.readIdentifier();
                Identifier entryId = buf.readIdentifier();

                //noinspection ConstantConditions
                RegistryUtils.remove(Registry.REGISTRIES.get(registryId), entryId);

                DynRegClient.removeRegisteredKey(registryId, entryId);
            }

            var addedEntriesCount = buf.readVarInt();

            LOGGER.info(" - Added {} entries", addedEntriesCount);

            for (int i = 0; i < addedEntriesCount; i++) {
                Identifier entryId = buf.readIdentifier();
                Identifier descId = buf.readIdentifier();
                EntryDescription<?> desc = EntryDescriptions.getDescParser(descId).apply(buf);

                registerDesc(entryId, desc);

                DynRegClient.addRegisteredKey(desc.registry().getKey().getValue(), entryId);
            }

            for (Registry<?> registry : Registry.REGISTRIES) {
                registry.freeze();
            }
        });
    }

    private static <T> void registerDesc(Identifier id, EntryDescription<T> desc) {
        Registry.register(desc.registry(), id, desc.create());
    }
}
