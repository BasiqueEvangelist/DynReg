package me.basiqueevangelist.dynreg.util;

import me.basiqueevangelist.dynreg.access.ExtendedIdList;
import net.minecraft.util.collection.IdList;

public final class IdListUtils {
    private IdListUtils() {

    }

    @SuppressWarnings("unchecked")
    public static <T> void remove(IdList<T> idList, T item) {
        ((ExtendedIdList<T>) idList).dynreg$remove(item);
    }
}
