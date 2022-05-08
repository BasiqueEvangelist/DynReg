package me.basiqueevangelist.dynreg.util;

import me.basiqueevangelist.dynreg.access.CleanablePalette;
import net.minecraft.world.chunk.Palette;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public final class PaletteUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/PaletteUtils");

    private PaletteUtils() {

    }

    @SuppressWarnings("unchecked")
    public static <T> void tryClean(Palette<T> palette, Function<T, T> fixer) {
        if (palette instanceof CleanablePalette<?>) {
            ((CleanablePalette<T>) palette).dynreg$cleanDeletedElements(fixer);
        } else {
            LOGGER.warn("Palette instance of {} isn't cleanable!", palette.getClass());
        }
    }
}
