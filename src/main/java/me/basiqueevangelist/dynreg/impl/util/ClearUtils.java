package me.basiqueevangelist.dynreg.impl.util;

import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ClearUtils {
    private ClearUtils() {

    }

    private static final Class<?> UNMODIFIABLE_SET_CLASS = Collections.unmodifiableSet(new HashSet<>()).getClass();

    @SafeVarargs
    public static <T> void clearMaps(T entry, Map<? extends T, ? extends T>... maps) {
        for (var map : maps) {
            map.remove(entry);
            map.entrySet().removeIf(x -> x.getValue() == entry);
        }
    }

    @SafeVarargs
    public static <T> void clearMapKeys(T entry, Map<? extends T, ?>... maps) {
        for (var map : maps) {
            map.remove(entry);
        }
    }

    @SafeVarargs
    public static <T> void clearMapValues(T entry, Map<?, ? extends T>... maps) {
        for (var map : maps) {
            map.entrySet().removeIf(x -> x.getValue() == entry);
        }
    }

    public static <T> Set<T> mutableifyAndRemove(Set<T> set, T element) {
        if (set instanceof ImmutableSet<T> || set.getClass() == UNMODIFIABLE_SET_CLASS) {
            set = new HashSet<>(set);
        }

        set.remove(element);

        return set;
    }
}
