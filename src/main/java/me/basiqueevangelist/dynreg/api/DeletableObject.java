package me.basiqueevangelist.dynreg.api;

public interface DeletableObject {
    default boolean wasDeleted() {
        throw new UnsupportedOperationException("Method wasn't implemented!");
    }
}
