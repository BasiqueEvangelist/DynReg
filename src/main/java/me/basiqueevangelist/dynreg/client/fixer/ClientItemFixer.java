package me.basiqueevangelist.dynreg.client.fixer;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ClientItemFixer {
    private ClientItemFixer() {

    }

    public static void init() {
        RegistryEntryAddedCallback.event(Registry.ITEM).register(ClientItemFixer::onItemAdded);
    }

    private static void onItemAdded(int rawId, Identifier id, Item value) {
        var client = MinecraftClient.getInstance();

        if (client != null && client.getItemRenderer() != null) {
            client.getItemRenderer().getModels().putModel(value, new ModelIdentifier(id, "inventory"));
        }
    }
}
