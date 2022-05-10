package me.basiqueevangelist.dynreg.util;

import me.basiqueevangelist.dynreg.mixin.fabric.ApiProviderHashMapAccessor;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap;

import java.util.Map;

public final class ApiLookupUtil {
    private ApiLookupUtil() {

    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> getActualMap(ApiProviderMap<K, V> providerMap) {
        return ((ApiProviderHashMapAccessor<K, V>) providerMap).getLookups();
    }
}
