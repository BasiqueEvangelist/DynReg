package me.basiqueevangelist.dynreg.mixin.fabric;

import me.basiqueevangelist.dynreg.data.RegistryEntryLoader;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(value = ResourceManagerHelperImpl.class, remap = false)
public class ResourceManagerHelperImplMixin {
    @Shadow @Final private static Map<ResourceType, ResourceManagerHelperImpl> registryMap;

    @Inject(method = "sort(Ljava/util/List;)V", at = @At("RETURN"))
    private void addRegistryEntryListener(List<ResourceReloader> listeners, CallbackInfo ci) {
        if (registryMap.get(ResourceType.SERVER_DATA) != (Object) this) return;

        listeners.add(0, RegistryEntryLoader.INSTANCE);
    }
}
