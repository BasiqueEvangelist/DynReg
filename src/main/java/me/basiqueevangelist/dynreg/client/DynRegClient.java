package me.basiqueevangelist.dynreg.client;

import me.basiqueevangelist.dynreg.client.fixer.ClientBlockFixer;
import net.fabricmc.api.ClientModInitializer;

public class DynRegClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientBlockFixer.init();
        DynRegClientNetworking.init();
    }
}
