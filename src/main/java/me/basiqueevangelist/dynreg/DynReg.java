package me.basiqueevangelist.dynreg;

import me.basiqueevangelist.dynreg.fixer.BlockFixer;
import me.basiqueevangelist.dynreg.fixer.ItemFixer;
import me.basiqueevangelist.dynreg.round.LoadedEntryHolder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.LoggerFactory;

public class DynReg implements ModInitializer {
    public static String MODID = "dynreg";

    public static MinecraftServer SERVER;

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    @Override
    public void onInitialize() {
        LoggerFactory.getLogger("DynReg").info("I have become DynReg, destroyer of immutability");

        ItemFixer.init();
        BlockFixer.init();

        LoadedEntryHolder.init();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            SERVER = server;
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            SERVER = null;
        });
    }
}
