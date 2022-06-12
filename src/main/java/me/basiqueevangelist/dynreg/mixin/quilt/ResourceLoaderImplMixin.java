package me.basiqueevangelist.dynreg.mixin.quilt;

import me.basiqueevangelist.dynreg.data.RegistryEntryLoader;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ResourceLoaderImpl.class)
public class ResourceLoaderImplMixin {
    @Inject(method = "sort(Lnet/minecraft/resource/ResourceType;Ljava/util/List;)V", at = @At("RETURN"))
    private static void addRegistryEntryListener(ResourceType type, List<ResourceReloader> reloaders, CallbackInfo ci) {
        if (type != ResourceType.SERVER_DATA) return;

        reloaders.add(0, RegistryEntryLoader.INSTANCE);
    }
}
