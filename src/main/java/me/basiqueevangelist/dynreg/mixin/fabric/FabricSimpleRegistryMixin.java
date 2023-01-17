package me.basiqueevangelist.dynreg.mixin.fabric;

import com.google.common.collect.BiMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.basiqueevangelist.dynreg.mixin.SimpleRegistryMixin;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SimpleRegistry.class, priority = 1100)
public class FabricSimpleRegistryMixin<T> {
    @SuppressWarnings({"ReferenceToMixin", "UnstableApiUsage"})
    @Shadow(remap = false)
    @Dynamic(mixin = net.fabricmc.fabric.mixin.registry.sync.SimpleRegistryMixin.class)
    private Object2IntMap<Identifier> fabric_prevIndexedEntries;
    @SuppressWarnings({"ReferenceToMixin", "UnstableApiUsage"})
    @Shadow(remap = false)
    @Dynamic(mixin = net.fabricmc.fabric.mixin.registry.sync.SimpleRegistryMixin.class)
    private BiMap<Identifier, RegistryEntry.Reference<T>> fabric_prevEntries;

    @SuppressWarnings("ReferenceToMixin")
    @Dynamic(mixin = SimpleRegistryMixin.class)
    @Inject(method = "dynreg$remove", at = @At("RETURN"), remap = false)
    private void mald(RegistryKey<T> key, CallbackInfo info) {
        if (fabric_prevEntries != null)
            fabric_prevEntries.remove(key.getValue());

        if (fabric_prevIndexedEntries != null)
            fabric_prevIndexedEntries.removeInt(key.getValue());
    }
}
