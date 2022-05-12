package me.basiqueevangelist.dynreg.mixin;

import me.basiqueevangelist.dynreg.access.ExtendedBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.Settings.class)
public class BlockSettingsMixin implements ExtendedBlockSettings {
    private MapColor dynreg$mapColor;

    @Inject(method = "<init>(Lnet/minecraft/block/Material;Lnet/minecraft/block/MapColor;)V", at = @At("RETURN"))
    private void setMapColor(Material material, MapColor mapColorProvider, CallbackInfo ci) {
        dynreg$mapColor = mapColorProvider;
    }

    @Inject(method = "mapColor", at = @At("HEAD"))
    private void setMapColor(MapColor color, CallbackInfoReturnable<AbstractBlock.Settings> cir) {
        dynreg$mapColor = color;
    }

    @Override
    public MapColor dynreg$getMapColor() {
        return dynreg$mapColor;
    }
}
