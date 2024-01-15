package me.basiqueevangelist.dynreg.fixer;

import me.basiqueevangelist.dynreg.api.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.util.ClearUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

public final class EntityFixer {
    private EntityFixer() {

    }

    public static void init() {
        RegistryEntryDeletedCallback.event(Registries.ENTITY_TYPE).register(EntityFixer::onEntityTypeDeleted);
    }

    private static void onEntityTypeDeleted(int rawId, RegistryEntry.Reference<EntityType<?>> entry) {
        EntityType<?> type = entry.value();

        ClearUtils.clearMapKeys(type, SpawnRestriction.RESTRICTIONS, SpawnEggItem.SPAWN_EGGS,
            DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY);
    }
}
