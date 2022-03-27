package me.basiqueevangelist.dynreg.network.block;

import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;

public interface BlockDescription extends EntryDescription<Block> {
    @Override
    Block create();

    @Override
    default Class<Block> entryType() {
        return Block.class;
    }

    @Override
    default Registry<Block> registry() {
        return Registry.BLOCK;
    }
}
