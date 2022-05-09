package me.basiqueevangelist.dynreg.client;

import me.basiqueevangelist.dynreg.client.event.PostLeaveRoundCallback;
import me.basiqueevangelist.dynreg.client.fixer.ClientBlockFixer;
import me.basiqueevangelist.dynreg.client.fixer.ClientItemFixer;
import me.basiqueevangelist.dynreg.client.round.ClientDynamicRound;
import me.basiqueevangelist.dynreg.holder.LoadedEntryHolder;
import me.basiqueevangelist.dynreg.round.RoundInternals;
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
    @Override
    public void onInitializeClient() {
        ClientBlockFixer.init();
        ClientItemFixer.init();
        DynRegClientNetworking.init();

        PostLeaveRoundCallback.EVENT.register(round -> {
            if (LoadedEntryHolder.getEntries().size() > 0) {
                round.addTask(() -> {
                    for (var data : LoadedEntryHolder.getEntries().values()) {
                        try {
                            data.entry().onRemoved();

                            for (RegistryKey<?> registeredKey : data.registeredKeys()) {
                                RegistryUtils.remove(registeredKey);
                            }

                        } catch (Exception e) {
                            LOGGER.error("Couldn't remove {}", data.entry().id(), e);
                        }
                    }
                    LoadedEntryHolder.getEntries().clear();
                });
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            var round = ClientDynamicRound.getRound();
            PostLeaveRoundCallback.EVENT.invoker().onClientDisconnect(round);
            round.run();
        });
    }
}
