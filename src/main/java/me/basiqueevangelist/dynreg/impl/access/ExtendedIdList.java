package me.basiqueevangelist.dynreg.impl.access;

import net.minecraft.util.collection.IdList;

public interface ExtendedIdList {
    void dynreg$clear();

    IdList<?> dynreg$copy();
}
