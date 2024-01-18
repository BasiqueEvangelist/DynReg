package me.basiqueevangelist.dynreg.impl.fixer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import me.basiqueevangelist.dynreg.api.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.api.event.RegistryFrozenCallback;
import me.basiqueevangelist.dynreg.impl.access.ExtendedIdList;
import me.basiqueevangelist.dynreg.impl.util.ApiLookupUtil;
import me.basiqueevangelist.dynreg.impl.util.ClearUtils;
import me.basiqueevangelist.dynreg.impl.util.DebugContext;
import me.basiqueevangelist.dynreg.impl.util.VersionTracker;
import me.basiqueevangelist.dynreg.mixin.fabriccommon.BlockApiLookupImplAccessor;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.IdList;
import net.minecraft.world.poi.PointOfInterestTypes;

import java.util.HashMap;

public final class BlockFixer {
    public static final VersionTracker BLOCKS_VERSION = new VersionTracker();

    public static IdList<BlockState> CURRENT_STATES_LIST = new IdList<>();

    private static boolean hasRemoved = false;

    private BlockFixer() {

    }

    public static void init() {
        RegistryEntryDeletedCallback.event(Registries.BLOCK).register(BlockFixer::onBlockDeleted);
        RegistryFrozenCallback.event(Registries.BLOCK).register(BlockFixer::onRegistryFrozen);

        DebugContext.addSupplied("dynreg:block_version", BLOCKS_VERSION::getVersion);
    }

    @SuppressWarnings("unchecked")
    private static void onRegistryFrozen() {
        if (hasRemoved) {
            hasRemoved = false;

            ((ExtendedIdList) Block.STATE_IDS).dynreg$clear();

            Int2ObjectMap<Block> sortedBlocks = new Int2ObjectRBTreeMap<>();

            Registries.BLOCK.forEach((t)
                -> sortedBlocks.put(Registries.BLOCK.getRawId(t), t));

            for (Block b : sortedBlocks.values()) {
                b.getStateManager().getStates().forEach(Block.STATE_IDS::add);
            }
        }

        CURRENT_STATES_LIST = (IdList<BlockState>) ((ExtendedIdList) Block.STATE_IDS).dynreg$copy();
    }

    private static void onBlockDeleted(int rawId, RegistryEntry.Reference<Block> entry) {
        hasRemoved = true;

        Block block = entry.value();

        for (BlockState state : block.getStateManager().getStates()) {
            ClearUtils.clearMapKeys(state,
                PointOfInterestTypes.POI_STATES_TO_TYPE);

            ClearUtils.clearMapValues(state,
                ShovelItem.PATH_STATES);

            ClearUtils.clearMaps(state,
                InfestedBlock.REGULAR_TO_INFESTED_STATE,
                InfestedBlock.INFESTED_TO_REGULAR_STATE);
        }

        for (BlockEntityType<?> beType : Registries.BLOCK_ENTITY_TYPE) {
            beType.blocks = ClearUtils.mutableifyAndRemove(beType.blocks, block);
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
