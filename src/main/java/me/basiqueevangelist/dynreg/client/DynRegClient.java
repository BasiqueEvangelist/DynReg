package me.basiqueevangelist.dynreg.client;

import me.basiqueevangelist.dynreg.client.event.PostLeaveRoundCallback;
import me.basiqueevangelist.dynreg.client.fixer.ClientBlockFixer;
import me.basiqueevangelist.dynreg.client.round.ClientDynamicRound;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DynRegClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/DynRegClient");
    private static final List<RegistryKey<?>> REGISTERED_KEYS = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        ClientBlockFixer.init();
        DynRegClientNetworking.init();

        PostLeaveRoundCallback.EVENT.register(round -> {
            if (REGISTERED_KEYS.size() > 0) {
                round.addTask(() -> {
                    for (var key : REGISTERED_KEYS) {
                        try {
                            RegistryUtils.remove(key);
                        } catch (Exception e) {
                            LOGGER.error("Couldn't remove {}", key, e);
                        }
                    }
                    REGISTERED_KEYS.clear();
                });
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            var round = ClientDynamicRound.getRound();
            PostLeaveRoundCallback.EVENT.invoker().onClientDisconnect(round);
            round.run();
        });
    }

    public static void addRegisteredKey(Identifier registryId, Identifier entryId) {
        REGISTERED_KEYS.add(RegistryKey.of(RegistryKey.ofRegistry(registryId), entryId));
    }

    public static void removeRegisteredKey(Identifier registryId, Identifier entryId) {
        REGISTERED_KEYS.add(RegistryKey.of(RegistryKey.ofRegistry(registryId), entryId));
    }
}
