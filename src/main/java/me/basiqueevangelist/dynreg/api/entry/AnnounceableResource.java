package me.basiqueevangelist.dynreg.api.entry;

/**
 * A resource that can be registered by a {@link RegistrationEntry}.
 *
 * <p>Used while {@linkplain RegistrationEntry#scan(EntryScanContext) scanning} to announce dependencies on content.
 */
public interface AnnounceableResource {
    default boolean isAlreadyPresent() {
        return false;
    }
}
