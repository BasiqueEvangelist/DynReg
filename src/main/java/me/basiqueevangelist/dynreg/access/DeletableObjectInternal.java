package me.basiqueevangelist.dynreg.access;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface DeletableObjectInternal extends DeletableObject {
    void dynreg$setDeleted(boolean value);

    void dynreg$setId(Identifier id);

    Identifier dynreg$getId();
}
