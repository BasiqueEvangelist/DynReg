package me.basiqueevangelist.dynreg.mixin.palette;

import me.basiqueevangelist.dynreg.access.CleanablePalette;
import me.basiqueevangelist.dynreg.access.DeletableObject;
import net.minecraft.world.chunk.ArrayPalette;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

@Mixin(ArrayPalette.class)
public class ArrayPaletteMixin<T> implements CleanablePalette<T> {

    @Shadow @Final private T[] array;

    @Override
    public void dynreg$cleanDeletedElements(Function<T, T> fixer) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] instanceof DeletableObject obj && obj.wasDeleted()) {
                array[i] = fixer.apply(array[i]);
            }
        }
    }
}
