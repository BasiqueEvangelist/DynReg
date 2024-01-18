package me.basiqueevangelist.dynreg.mixin.palette;

import me.basiqueevangelist.dynreg.impl.access.ExtendedIdListPalette;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteResizeListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.List;

@Mixin(targets = "net/minecraft/world/chunk/PalettedContainer$PaletteProvider$2")
public class BiomePaletteProviderMixin {
    @ModifyArg(
        method = "createDataProvider",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/chunk/PalettedContainer$DataProvider;<init>(Lnet/minecraft/world/chunk/Palette$Factory;I)V"
        ),
        slice = @Slice(
            from = @At(
                value = "FIELD",
                target = "Lnet/minecraft/world/chunk/PalettedContainer$PaletteProvider;ID_LIST:Lnet/minecraft/world/chunk/Palette$Factory;"
            )
        )
    )
    private Palette.Factory markAsBiome(Palette.Factory factory) {
        return new Palette.Factory() {
            @Override
            public <A> Palette<A> create(int bits, IndexedIterable<A> idList, PaletteResizeListener<A> listener, List<A> list) {
                var palette = factory.create(bits, idList, listener, list);

                if (palette instanceof ExtendedIdListPalette ext)
                    ext.dynreg$markAsBiome();

                return palette;
            }
        };
    }
}
