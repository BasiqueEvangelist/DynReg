package me.basiqueevangelist.dynreg.impl.access;

import java.util.function.Function;

public interface CleanablePalette<T> {
    void dynreg$cleanDeletedElements(Function<T, T> defaultElement);
}
