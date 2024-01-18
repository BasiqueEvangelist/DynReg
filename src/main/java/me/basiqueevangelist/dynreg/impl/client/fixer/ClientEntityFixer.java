package me.basiqueevangelist.dynreg.impl.client.fixer;

import me.basiqueevangelist.dynreg.api.event.RegistryEntryDeletedCallback;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

public class ClientEntityFixer {
    private ClientEntityFixer() {

    }

    public static void init() {
        RegistryEntryDeletedCallback.event(Registries.ENTITY_TYPE).register(ClientEntityFixer::onEntityDeleted);
    }

    private static void onEntityDeleted(int rawId, RegistryEntry.Reference<EntityType<?>> entry) {
        EntityRenderers.RENDERER_FACTORIES.remove(entry.value());
    }
}
