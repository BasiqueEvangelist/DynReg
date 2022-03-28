package me.basiqueevangelist.dynreg.access;

public interface DeletableObject {
    default boolean wasDeleted() {
        throw new UnsupportedOperationException("Method wasn't implemented!");
    }
}
