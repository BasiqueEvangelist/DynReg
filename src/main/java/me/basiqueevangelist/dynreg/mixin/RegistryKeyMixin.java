package me.basiqueevangelist.dynreg.mixin;

import me.basiqueevangelist.dynreg.api.entry.AnnounceableResource;
import me.basiqueevangelist.dynreg.impl.util.RegistryUtils;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RegistryKey.class)
public class RegistryKeyMixin implements AnnounceableResource {
    @SuppressWarnings("unchecked")
    @Override
    public boolean isAlreadyPresent() {
        var key = (RegistryKey<Object>) (Object) this;
        return RegistryUtils.getRegistryOf(key).contains(key);
    }
}
