package me.basiqueevangelist.dynreg.fixer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.BlockState;

public class RemovedStateIdList {
    public RemovedStateIdList() {
        BlockFixer.REMOVED_ID_LISTS.add(this);
    }

    private final Int2ObjectMap<BlockState> removedStateIds = new Int2ObjectOpenHashMap<>();

    public Int2ObjectMap<BlockState> getRemovedStateIds() {
        return removedStateIds;
    }
}
