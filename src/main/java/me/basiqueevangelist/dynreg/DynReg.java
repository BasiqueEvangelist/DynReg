package me.basiqueevangelist.dynreg;

import me.basiqueevangelist.dynreg.compat.CompatLoader;
import me.basiqueevangelist.dynreg.fixer.BlockFixer;
import me.basiqueevangelist.dynreg.fixer.GlobalFixer;
import me.basiqueevangelist.dynreg.fixer.ItemFixer;
import me.basiqueevangelist.dynreg.holder.LoadedEntryHolder;
import me.basiqueevangelist.dynreg.holder.ReactiveEntryTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.LoggerFactory;

public class DynReg implements ModInitializer {
    public static final String MODID = "dynreg";

    public static final boolean DEBUG = Boolean.getBoolean("dynreg.debug");

    public static MinecraftServer SERVER;

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    @Override
    public void onInitialize() {
        LoggerFactory.getLogger("DynReg").info("I have become DynReg, destroyer of immutability");

        ItemFixer.init();
        BlockFixer.init();
        GlobalFixer.init();

        ReactiveEntryTracker.init();
        LoadedEntryHolder.init();
        CompatLoader.init();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> SERVER = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> SERVER = null);

//        if (DEBUG) {
//            ((ExtendedRegistry<?>) Registry.BLOCK).dynreg$installStackTracingMap();
//        }
    }
}
