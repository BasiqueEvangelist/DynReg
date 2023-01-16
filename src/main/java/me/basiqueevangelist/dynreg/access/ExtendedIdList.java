package me.basiqueevangelist.dynreg.access;

import net.minecraft.util.collection.IdList;

public interface ExtendedIdList {
    void dynreg$clear();

    IdList<?> dynreg$copy();
}
