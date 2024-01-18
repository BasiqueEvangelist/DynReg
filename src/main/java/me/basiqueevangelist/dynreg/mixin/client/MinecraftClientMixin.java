// Taken from <https://github.com/wisp-forest/owo-lib/blob/1.19.3/src/main/java/io/wispforest/owo/mixin/MinecraftClientMixin.java>

package me.basiqueevangelist.dynreg.mixin.client;

import me.basiqueevangelist.dynreg.impl.data.StaticDataLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinecraftClient.class, priority = 0)
public class MinecraftClientMixin {

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Group(name = "clientFreezeHooks", min = 1, max = 1)
    @Inject(method = "<init>", at = @At(value = "INVOKE", remap = false,
        target = "Lnet/fabricmc/loader/impl/game/minecraft/Hooks;startClient(Ljava/io/File;Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private void afterFabricHook(RunArgs args, CallbackInfo ci) {
        StaticDataLoader.init();
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Group(name = "clientFreezeHooks", min = 1, max = 1)
    @Inject(method = "<init>", at = @At(value = "INVOKE", remap = false,
        target = "Lorg/quiltmc/loader/impl/game/minecraft/Hooks;startClient(Ljava/io/File;Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private void afterQuiltHook(RunArgs args, CallbackInfo ci) {
        StaticDataLoader.init();
    }

}
