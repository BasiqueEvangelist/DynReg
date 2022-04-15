package me.basiqueevangelist.dynreg.fixer;

import me.basiqueevangelist.dynreg.debug.DebugContext;
import me.basiqueevangelist.dynreg.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.util.ClearUtils;
import me.basiqueevangelist.dynreg.util.IdListUtils;
import me.basiqueevangelist.dynreg.util.VersionTracker;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.HashMap;

public final class BlockFixer {
    public static VersionTracker BLOCKS_VERSION = new VersionTracker();

    private BlockFixer() {

    }

    public static void init() {
        RegistryEntryDeletedCallback.event(Registry.BLOCK).register(BlockFixer::onBlockDeleted);

        DebugContext.addSupplied("dynreg:block_version", () -> BLOCKS_VERSION.getVersion());
    }

    private static void onBlockDeleted(int rawId, RegistryEntry.Reference<Block> entry) {
        Block block = entry.value();

        for (BlockState state : block.getStateManager().getStates()) {
            IdListUtils.remove(Block.STATE_IDS, state);

            ClearUtils.clearMapKeys(state,
                PointOfInterestType.BLOCK_STATE_TO_POINT_OF_INTEREST_TYPE);

            ClearUtils.clearMapValues(state,
                ShovelItem.PATH_STATES);

            ClearUtils.clearMaps(state,
                InfestedBlock.REGULAR_TO_INFESTED_STATE,
                InfestedBlock.INFESTED_TO_REGULAR_STATE);
        }

        Oxidizable.OXIDATION_LEVEL_INCREASES.get().remove(block);
        Oxidizable.OXIDATION_LEVEL_DECREASES.get().remove(block);
        HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().remove(block);
        HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().remove(block);

        if (!(AxeItem.STRIPPED_BLOCKS instanceof HashMap<?, ?>)) {
            AxeItem.STRIPPED_BLOCKS = new HashMap<>(AxeItem.STRIPPED_BLOCKS);
        }

        ClearUtils.clearMapKeys(block,
            Item.BLOCK_ITEMS,
            Oxidizable.OXIDATION_LEVEL_INCREASES.get(),
            Oxidizable.OXIDATION_LEVEL_DECREASES.get(),
            HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get(),
            HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get(),
            HoeItem.TILLING_ACTIONS,
            ShovelItem.PATH_STATES);
        ClearUtils.clearMaps(block,
            AxeItem.STRIPPED_BLOCKS,
            CandleCakeBlock.CANDLES_TO_CANDLE_CAKES,
            FlowerPotBlock.CONTENT_TO_POTTED,
            InfestedBlock.REGULAR_TO_INFESTED_BLOCK);

        BLOCKS_VERSION.bumpVersion();
    }
}
