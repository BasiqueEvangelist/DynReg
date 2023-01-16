package me.basiqueevangelist.dynreg.mixin.palette;

import me.basiqueevangelist.dynreg.access.CleanablePalette;
import me.basiqueevangelist.dynreg.access.DeletableObject;
import net.minecraft.world.chunk.SingularPalette;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

@Mixin(SingularPalette.class)
public class SingularPaletteMixin<T> implements CleanablePalette<T> {
    @Shadow
    private @Nullable T entry;

    @Override
    public void dynreg$cleanDeletedElements(Function<T, T> fixer) {
        if (entry instanceof DeletableObject obj && obj.wasDeleted()) {
            entry = fixer.apply(entry);
        }
    }
}
