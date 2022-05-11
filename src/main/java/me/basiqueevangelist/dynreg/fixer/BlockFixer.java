package me.basiqueevangelist.dynreg.fixer;

import me.basiqueevangelist.dynreg.debug.DebugContext;
import me.basiqueevangelist.dynreg.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.mixin.fabric.BlockApiLookupImplAccessor;
import me.basiqueevangelist.dynreg.util.ApiLookupUtil;
import me.basiqueevangelist.dynreg.util.ClearUtils;
import me.basiqueevangelist.dynreg.util.IdListUtils;
import me.basiqueevangelist.dynreg.util.VersionTracker;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.WeakHashMap;

public final class BlockFixer {
    public static VersionTracker BLOCKS_VERSION = new VersionTracker();

    static final Set<RemovedStateIdList> REMOVED_ID_LISTS = Collections.newSetFromMap(new WeakHashMap<>());

    private BlockFixer() {

    }

    public static void init() {
        RegistryEntryDeletedCallback.event(Registry.BLOCK).register(BlockFixer::onBlockDeleted);

        DebugContext.addSupplied("dynreg:block_version", () -> BLOCKS_VERSION.getVersion());
    }

    @SuppressWarnings("unchecked")
    private static void onBlockDeleted(int rawId, RegistryEntry.Reference<Block> entry) {
        Block block = entry.value();

        for (BlockState state : block.getStateManager().getStates()) {
            for (RemovedStateIdList list : REMOVED_ID_LISTS) {
                list.getRemovedStateIds().put(Block.STATE_IDS.getRawId(state), state);
            }

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

        for (var lookup : BlockApiLookupImplAccessor.getLOOKUPS()) {
            var apiProviderMap = ((BlockApiLookupImplAccessor) lookup).getProviderMap();
            var map = ApiLookupUtil.getActualMap(apiProviderMap);
            map.remove(block);
        }

        BLOCKS_VERSION.bumpVersion();
    }

}
