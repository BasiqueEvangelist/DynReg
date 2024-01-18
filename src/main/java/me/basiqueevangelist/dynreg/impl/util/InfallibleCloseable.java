package me.basiqueevangelist.dynreg.impl.util;

public interface InfallibleCloseable extends AutoCloseable {
    @Override
    void close();
}
