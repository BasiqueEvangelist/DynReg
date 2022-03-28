package me.basiqueevangelist.dynreg.network.item;

import me.basiqueevangelist.dynreg.network.EntryDescription;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public interface ItemDescription extends EntryDescription<Item> {
    @Override
    Item create();

    @Override
    default Registry<? super Item> registry() {
        return Registry.ITEM;
    }
}
