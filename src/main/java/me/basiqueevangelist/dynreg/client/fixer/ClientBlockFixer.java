package me.basiqueevangelist.dynreg.client.fixer;

import me.basiqueevangelist.dynreg.event.RegistryEntryDeletedCallback;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

public final class ClientBlockFixer {
    private ClientBlockFixer() {

    }

    public static void init() {
        RegistryEntryDeletedCallback.event(Registries.BLOCK).register(ClientBlockFixer::onBlockDeleted);
    }

    private static void onBlockDeleted(int rawId, RegistryEntry.Reference<Block> entry) {
        Block block = entry.value();

        RenderLayers.BLOCKS.remove(block);
    }
}
