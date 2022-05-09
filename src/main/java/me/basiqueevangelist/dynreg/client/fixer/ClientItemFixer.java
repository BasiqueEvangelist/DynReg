package me.basiqueevangelist.dynreg.client.fixer;

import me.basiqueevangelist.dynreg.event.RegistryEntryDeletedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;

public class ClientItemFixer {
    private ClientItemFixer() {

    }

    public static void init() {
        RegistryEntryAddedCallback.event(Registry.ITEM).register(ClientItemFixer::onBlockAdded);
        RegistryEntryDeletedCallback.event(Registry.ITEM).register(ClientItemFixer::onBlockDeleted);
    }

    private static void onBlockAdded(int rawId, Identifier id, Item value) {
        var client = MinecraftClient.getInstance();

        if (client != null && client.getItemRenderer() != null) {
            client.getItemRenderer().getModels().putModel(value, new ModelIdentifier(id, "inventory"));
        }
    }

    private static void onBlockDeleted(int rawId, RegistryEntry.Reference<Item> entry) {
        Item item = entry.value();

        MinecraftClient.getInstance().getItemRenderer().getModels().modelIds.remove(Item.getRawId(item), new ModelIdentifier(entry.registryKey().getValue(), "inventory"));
    }
}
