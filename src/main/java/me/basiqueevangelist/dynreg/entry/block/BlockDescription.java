package me.basiqueevangelist.dynreg.entry.block;

import me.basiqueevangelist.dynreg.entry.EntryDescription;
import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;

public interface BlockDescription extends EntryDescription<Block> {
    @Override
    Block create();

    @Override
    default Registry<? super Block> registry() {
        return Registry.BLOCK;
    }
}
