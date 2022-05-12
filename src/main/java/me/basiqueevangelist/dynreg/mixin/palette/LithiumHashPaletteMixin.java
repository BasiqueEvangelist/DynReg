package me.basiqueevangelist.dynreg.mixin.palette;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import me.basiqueevangelist.dynreg.access.CleanablePalette;
import me.basiqueevangelist.dynreg.access.DeletableObject;
import me.jellysquid.mods.lithium.common.world.chunk.LithiumHashPalette;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

@Pseudo
@Mixin(value = LithiumHashPalette.class, remap = false)
public class LithiumHashPaletteMixin<T> implements CleanablePalette<T> {

    @Shadow private T[] entries;

    @Shadow @Final private Reference2IntMap<T> table;

    @Override
    public void dynreg$cleanDeletedElements(Function<T, T> fixer) {
        for (int i = 0; i < entries.length; i++) {
            if (entries[i] instanceof DeletableObject obj && obj.wasDeleted())
                entries[i] = fixer.apply(entries[i]);
        }

        table.reference2IntEntrySet().removeIf(x -> x.getKey() instanceof DeletableObject obj && obj.wasDeleted());
    }
}
