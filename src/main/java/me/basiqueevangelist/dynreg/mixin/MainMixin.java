// Taken from <https://github.com/wisp-forest/owo-lib/blob/1.19.3/src/main/java/io/wispforest/owo/mixin/MainMixin.java>.

package me.basiqueevangelist.dynreg.mixin;

import me.basiqueevangelist.dynreg.data.StaticDataLoader;
import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Main.class, priority = 0)
public class MainMixin {
    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Group(name = "serverFreezeHooks", min = 1, max = 1)
    @Inject(method = "main", at = @At(value = "INVOKE", remap = false,
        target = "Lnet/fabricmc/loader/impl/game/minecraft/Hooks;startServer(Ljava/io/File;Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private static void afterFabricHook(CallbackInfo ci) {
        StaticDataLoader.init();
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Group(name = "serverFreezeHooks", min = 1, max = 1)
    @Inject(method = "main", at = @At(value = "INVOKE", remap = false,
        target = "Lorg/quiltmc/loader/impl/game/minecraft/Hooks;startServer(Ljava/io/File;Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private static void afterQuiltHook(CallbackInfo ci) {
        StaticDataLoader.init();
    }

}
