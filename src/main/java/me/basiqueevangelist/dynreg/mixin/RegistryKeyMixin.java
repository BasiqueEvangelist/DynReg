package me.basiqueevangelist.dynreg.mixin;

import me.basiqueevangelist.dynreg.entry.AnnounceableResource;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RegistryKey.class)
public class RegistryKeyMixin implements AnnounceableResource {
    @SuppressWarnings("unchecked")
    @Override
    public boolean dynreg$isAlreadyPresent() {
        var key = (RegistryKey<Object>) (Object) this;
        return RegistryUtils.getRegistryOf(key).contains(key);
    }
}
