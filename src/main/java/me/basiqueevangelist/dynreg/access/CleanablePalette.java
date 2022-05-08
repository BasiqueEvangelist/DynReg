package me.basiqueevangelist.dynreg.access;

import java.util.function.Function;

public interface CleanablePalette<T> {
    void dynreg$cleanDeletedElements(Function<T, T> defaultElement);
}
