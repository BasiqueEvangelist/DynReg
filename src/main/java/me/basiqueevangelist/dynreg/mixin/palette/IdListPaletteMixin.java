package me.basiqueevangelist.dynreg.mixin.palette;

import me.basiqueevangelist.dynreg.access.CleanablePalette;
import net.minecraft.world.chunk.IdListPalette;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Function;

@Mixin(IdListPalette.class)
public class IdListPaletteMixin<T> implements CleanablePalette<T> {
    @Override
    public void dynreg$cleanDeletedElements(Function<T, T> defaultElement) {
        // Do nothing.
    }
}
