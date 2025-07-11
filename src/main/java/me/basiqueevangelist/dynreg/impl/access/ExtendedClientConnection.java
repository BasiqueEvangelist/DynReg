package me.basiqueevangelist.dynreg.impl.access;

public interface ExtendedClientConnection {
    void dynreg$markAsResync();

    boolean dynreg$isResync();
}
