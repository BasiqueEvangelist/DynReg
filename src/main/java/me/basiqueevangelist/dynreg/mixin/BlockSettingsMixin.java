package me.basiqueevangelist.dynreg.mixin;

import me.basiqueevangelist.dynreg.impl.access.ExtendedBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.Settings.class)
public class BlockSettingsMixin implements ExtendedBlockSettings {
    private MapColor dynreg$mapColor;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void setMapColor(CallbackInfo ci) {
        dynreg$mapColor = MapColor.CLEAR;
    }

    @Inject(method = "mapColor(Lnet/minecraft/util/DyeColor;)Lnet/minecraft/block/AbstractBlock$Settings;", at = @At("HEAD"))
    private void setMapColor(DyeColor color, CallbackInfoReturnable<AbstractBlock.Settings> cir) {
        dynreg$mapColor = color.getMapColor();
    }

    @Inject(method = "mapColor(Lnet/minecraft/block/MapColor;)Lnet/minecraft/block/AbstractBlock$Settings;", at = @At("HEAD"))
    private void setMapColor(MapColor color, CallbackInfoReturnable<AbstractBlock.Settings> cir) {
        dynreg$mapColor = color;
    }

    @Override
    public MapColor dynreg$getMapColor() {
        return dynreg$mapColor;
    }
}
