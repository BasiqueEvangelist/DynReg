package me.basiqueevangelist.dynreg.entry.item;

import me.basiqueevangelist.dynreg.entry.EntryDescription;
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
