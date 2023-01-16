package me.basiqueevangelist.dynreg.mixin.palette;

import me.basiqueevangelist.dynreg.access.CleanablePalette;
import me.basiqueevangelist.dynreg.access.DeletableObject;
import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.world.chunk.BiMapPalette;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

@Mixin(BiMapPalette.class)
public class BiMapPaletteMixin<T> implements CleanablePalette<T> {
    @Shadow
    @Final
    private Int2ObjectBiMap<T> map;

    @Override
    public void dynreg$cleanDeletedElements(Function<T, T> fixer) {
        for (int i = 0; i < map.values.length; i++) {
            if (map.values[i] instanceof DeletableObject obj && obj.wasDeleted())
                map.values[i] = fixer.apply(map.values[i]);
        }

        for (int i = 0; i < map.idToValues.length; i++) {
            if (map.idToValues[i] instanceof DeletableObject obj && obj.wasDeleted())
                map.idToValues[i] = fixer.apply(map.idToValues[i]);
        }
    }
}
