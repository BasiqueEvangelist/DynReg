package me.basiqueevangelist.dynreg.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.basiqueevangelist.dynreg.access.ExtendedIdList;
import net.minecraft.util.collection.IdList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(IdList.class)
public abstract class IdListMixin<T> implements ExtendedIdList {
    @Shadow @Final private List<T> list;

    @Shadow @Final private Object2IntMap<T> idMap;

    @Shadow private int nextId;

    @Override
    public void dynreg$clear() {
        list.clear();
        idMap.clear();
        nextId = 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IdList<T> dynreg$copy() {
        IdList<T> copy = new IdList<>(0);
        var ref = (IdListMixin<T>)(Object) copy;

        ref.list.addAll(list);
        ref.idMap.putAll(idMap);
        ref.nextId = nextId;

        return copy;
    }
}
