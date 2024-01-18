package me.basiqueevangelist.dynreg.impl.util;

import net.minecraft.util.collection.IndexedIterable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public record DefaultedIndexIterable<T>(IndexedIterable<T> wrapped,
                                        T defaultValue) implements IndexedIterable<T> {
    @Override
    public int getRawId(T value) {
        int id = wrapped.getRawId(value);

        if (id == IndexedIterable.ABSENT_RAW_ID)
            return wrapped.getRawId(defaultValue);
        else
            return id;
    }

    @Nullable
    @Override
    public T get(int index) {
        T value = wrapped.get(index);

        if (value == null)
            return defaultValue;
        else
            return value;
    }

    @Override
    public int size() {
        return wrapped.size();
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return wrapped.iterator();
    }
}
