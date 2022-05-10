package me.basiqueevangelist.dynreg.mixin.fabric;

import net.fabricmc.fabric.api.lookup.v1.custom.ApiLookupMap;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.impl.lookup.item.ItemApiLookupImpl;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemApiLookupImpl.class)
public interface ItemApiLookupImplAccessor {
    @Accessor
    static ApiLookupMap<ItemApiLookup<?, ?>> getLOOKUPS() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    ApiProviderMap<Item, ItemApiLookup.ItemApiProvider<?, ?>> getProviderMap();
}
