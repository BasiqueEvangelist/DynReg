package me.basiqueevangelist.dynreg.access;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface DeletableObjectInternal extends DeletableObject {
    void markAsDeleted();
}
