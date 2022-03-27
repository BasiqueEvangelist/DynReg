package me.basiqueevangelist.dynreg.mixin;

import me.basiqueevangelist.dynreg.access.ExtendedRegistryEntryReference;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RegistryEntry.Reference.class)
public class ReferenceRegistryEntryMixin implements ExtendedRegistryEntryReference {
    private boolean dynreg$poisoned = false;

    @Override
    public void dynreg$poison() {
        dynreg$poisoned = true;
    }

    // Methods will be transformed via ASM.
    private void dynreg$checkPoisoned() {
        if (dynreg$poisoned)
            throw new IllegalStateException("Tried to use poisoned registry entry!");
    }
}
