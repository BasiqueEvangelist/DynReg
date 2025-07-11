package me.basiqueevangelist.dynreg.impl;

import me.basiqueevangelist.dynreg.impl.compat.CompatLoader;
import me.basiqueevangelist.dynreg.impl.fixer.*;
import me.basiqueevangelist.dynreg.impl.holder.LoadedEntryHolder;
import me.basiqueevangelist.dynreg.impl.holder.ReactiveEntryTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.LoggerFactory;

public class DynReg implements ModInitializer {
    public static final String MODID = "dynreg";

    public static final boolean DEBUG = Boolean.getBoolean("dynreg.debug");

    public static MinecraftServer SERVER;

    public static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }

    @Override
    public void onInitialize() {
        LoggerFactory.getLogger("DynReg").info("I have become DynReg, destroyer of immutability");

        ItemFixer.init();
        BlockFixer.init();
        GlobalFixer.init();
        StatusEffectFixer.init();
        if (!FabricLoader.getInstance().isModLoaded("quilt_base"))
            FabricGlobalFixer.init();

        ReactiveEntryTracker.init();
        LoadedEntryHolder.init();
        CompatLoader.init();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> SERVER = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> SERVER = null);

        PayloadTypeRegistry.configurationS2C().register(RoundFinishedS2CPacket.ID, RoundFinishedS2CPacket.PACKET_CODEC);

    }
}
