package me.basiqueevangelist.dynreg.entry;

/**
 * Anything you can depend on, like a registry entry
 */
public interface AnnounceableResource {
    default boolean dynreg$isAlreadyPresent() {
        return false;
    }
}
