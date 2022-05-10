package me.basiqueevangelist.dynreg.mixin.fabric;

import net.fabricmc.fabric.impl.lookup.custom.ApiProviderHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ApiProviderHashMap.class)
public interface ApiProviderHashMapAccessor<K, V> {
    @Accessor
    Map<K, V> getLookups();
}
