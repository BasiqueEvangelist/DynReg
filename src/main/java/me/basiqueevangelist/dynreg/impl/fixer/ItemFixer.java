package me.basiqueevangelist.dynreg.impl.fixer;

import me.basiqueevangelist.dynreg.api.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.impl.util.ApiLookupUtil;
import me.basiqueevangelist.dynreg.impl.util.ClearUtils;
import me.basiqueevangelist.dynreg.impl.util.DebugContext;
import me.basiqueevangelist.dynreg.impl.util.VersionTracker;
import me.basiqueevangelist.dynreg.mixin.fabriccommon.ItemApiLookupImplAccessor;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.DispenserBlock;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.recipe.FireworkStarRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

public final class ItemFixer {
    public static final VersionTracker ITEMS_VERSION = new VersionTracker();

    private ItemFixer() {

    }

    public static void init() {
        RegistryEntryDeletedCallback.event(Registries.ITEM).register(ItemFixer::onItemDeleted);

        DebugContext.addSupplied("dynreg:item_version", ITEMS_VERSION::getVersion);
    }

    private static void onItemDeleted(int rawId, RegistryEntry.Reference<Item> entry) {
        Item item = entry.value();

        ClearUtils.clearMapValues(item,
            Item.BLOCK_ITEMS,
            SpawnEggItem.SPAWN_EGGS);

        ClearUtils.clearMapKeys(item,
            DispenserBlock.BEHAVIORS,
            FireworkStarRecipe.TYPE_MODIFIER_MAP);

        CompostingChanceRegistry.INSTANCE.remove(item);
        FuelRegistry.INSTANCE.remove(item);

        for (var lookup : ItemApiLookupImplAccessor.getLOOKUPS()) {
            var apiProviderMap = ((ItemApiLookupImplAccessor) lookup).getProviderMap();
            var map = ApiLookupUtil.getActualMap(apiProviderMap);
            map.remove(item);
        }

        ITEMS_VERSION.bumpVersion();
    }
}
