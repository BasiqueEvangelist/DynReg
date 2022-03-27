package me.basiqueevangelist.dynreg.client;

import com.mojang.datafixers.util.Pair;
import me.basiqueevangelist.dynreg.network.DynRegNetworking;
import me.basiqueevangelist.dynreg.network.EntryDescriptions;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynRegClientNetworking {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/ClientNetworking");
    private static long ROUND_RECEIVE_TIME = 0;

    @SuppressWarnings("unchecked")
    public static void init() {
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

            var removedEntries = buf.readList((buf2) -> {
                Identifier registryId = buf2.readIdentifier();
                Identifier entryId = buf2.readIdentifier();

                return new Pair<>(registryId, entryId);
            });

            LOGGER.info(" - Removed {} entries", removedEntries.size());

            for (var entry : removedEntries) {
                //noinspection ConstantConditions
                RegistryUtils.remove(Registry.REGISTRIES.get(entry.getFirst()), entry.getSecond());
            }

            var addedEntries = buf.readMap(PacketByteBuf::readIdentifier, (buf2) -> {
                Identifier id = buf2.readIdentifier();
                return EntryDescriptions.getDescParser(id).apply(buf2);
            });

            LOGGER.info(" - Added {} entries", addedEntries.size());

            for (var entry : addedEntries.entrySet()) {
                Registry.register((Registry<Object>) entry.getValue().registry(), entry.getKey(), (Object) entry.getValue().create());
            }

            for (Registry<?> registry : Registry.REGISTRIES) {
                registry.freeze();
            }
        });
    }
}
