package me.basiqueevangelist.dynreg.mixin;

import net.minecraft.world.ChunkSerializer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {
    // Shouldn't be needed anymore
//    @ModifyArg(method = {"<clinit>"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/PalettedContainer;method_44343(Lnet/minecraft/util/collection/IndexedIterable;Lcom/mojang/serialization/Codec;Lnet/minecraft/world/chunk/PalettedContainer$PaletteProvider;Ljava/lang/Object;)Lcom/mojang/serialization/Codec;"))
//    private static IndexedIterable<BlockState> wrapIterable(IndexedIterable<BlockState> idList) {
//        return new DefaultedIndexIterable<>(idList, Blocks.AIR.getDefaultState());
//    }
//
//    @ModifyArg(method = "deserialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/PalettedContainer;<init>(Lnet/minecraft/util/collection/IndexedIterable;Ljava/lang/Object;Lnet/minecraft/world/chunk/PalettedContainer$PaletteProvider;)V", ordinal = 0))
//    private static IndexedIterable<BlockState> wrapIterable2(IndexedIterable<BlockState> iterable) {
//        return new DefaultedIndexIterable<>(iterable, Blocks.AIR.getDefaultState());
//    }
}
