package me.basiqueevangelist.dynreg.fixer;

import me.basiqueevangelist.dynreg.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.util.ClearUtils;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.DispenserBlock;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.recipe.FireworkStarRecipe;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;

public final class ItemFixer {
    private ItemFixer() {

    }

    public static void init() {
        RegistryEntryDeletedCallback.event(Registry.ITEM).register(ItemFixer::onItemDeleted);
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
    }
}
