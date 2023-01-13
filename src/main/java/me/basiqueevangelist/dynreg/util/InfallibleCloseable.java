package me.basiqueevangelist.dynreg.util;

public interface InfallibleCloseable extends AutoCloseable {
    @Override
    void close();
}
