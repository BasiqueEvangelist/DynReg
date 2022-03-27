package me.basiqueevangelist.dynreg.client.fixer;

import me.basiqueevangelist.dynreg.event.RegistryEntryDeletedCallback;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;

public final class ClientBlockFixer {
    private ClientBlockFixer() {

    }

    public static void init() {
        RegistryEntryDeletedCallback.event(Registry.BLOCK).register(ClientBlockFixer::onBlockDeleted);
    }

    private static void onBlockDeleted(int rawId, RegistryEntry.Reference<Block> entry) {
        Block block = entry.value();

        RenderLayers.BLOCKS.remove(block);
    }
}
