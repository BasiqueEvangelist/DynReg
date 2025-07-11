package me.basiqueevangelist.dynreg.mixin;

import com.google.common.collect.Iterators;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import me.basiqueevangelist.dynreg.api.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.api.event.RegistryFrozenCallback;
import me.basiqueevangelist.dynreg.impl.access.ExtendedRegistry;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(value = SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements ExtendedRegistry<T>, Registry<T> {
    @Shadow private boolean frozen;
    @Shadow
    @Nullable
    private Map<T, RegistryEntry.Reference<T>> intrusiveValueToEntry;

    @Shadow
    public abstract Optional<RegistryEntry.Reference<T>> getEntry(RegistryKey<T> key);

    @Shadow
    @Final
    private Reference2IntMap<T> entryToRawId;
    @Shadow
    @Final
    private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;
    @Mutable
    @Shadow
    @Final
    private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;
    @Shadow
    @Final
    private Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry;
    @Shadow
    @Final
    private Map<T, RegistryEntry.Reference<T>> valueToEntry;

    @Shadow @Final private Map<RegistryKey<T>, RegistryEntryInfo> keyToEntryInfo;
    @SuppressWarnings("unchecked") private final Event<RegistryEntryDeletedCallback<T>> dynreg$entryDeletedEvent = EventFactory.createArrayBacked(RegistryEntryDeletedCallback.class, callbacks -> (rawId, entry) -> {
        for (var callback : callbacks) {
            callback.onEntryDeleted(rawId, entry);
        }

        if (entry.value() instanceof RegistryEntryDeletedCallback<?> callback)
            ((RegistryEntryDeletedCallback<T>) callback).onEntryDeleted(rawId, entry);
    });
    private final Event<RegistryFrozenCallback<T>> dynreg$registryFrozenEvent = EventFactory.createArrayBacked(RegistryFrozenCallback.class, callbacks -> () -> {
        for (var callback : callbacks) {
            callback.onRegistryFrozen();
        }
    });
    private final IntSortedSet dynreg$freeIds = new IntAVLTreeSet();
    private boolean dynreg$intrusive;

    @Override
    public Event<RegistryEntryDeletedCallback<T>> dynreg$getEntryDeletedEvent() {
        return dynreg$entryDeletedEvent;
    }

    @Override
    public Event<RegistryFrozenCallback<T>> dynreg$getRegistryFrozenEvent() {
        return dynreg$registryFrozenEvent;
    }

    @Inject(method = "<init>(Lnet/minecraft/registry/RegistryKey;Lcom/mojang/serialization/Lifecycle;Z)V", at = @At("TAIL"))
    private void saveIntrusiveness(RegistryKey<?> key, Lifecycle lifecycle, boolean intrusive, CallbackInfo ci) {
        dynreg$intrusive = intrusive;
    }

    @Override
    public void dynreg$remove(RegistryKey<T> key) {
        if (frozen) {
            throw new IllegalStateException("Registry is frozen (trying to remove key " + key + ")");
        }

        RegistryEntry.Reference<T> entry = getEntry(key).orElseThrow();

        int rawId = entryToRawId.getInt(entry.value());
        dynreg$entryDeletedEvent.invoker().onEntryDeleted(rawId, entry);

        rawIdToEntry.set(rawId, null);
        entryToRawId.removeInt(entry.value());
        idToEntry.remove(key.getValue());
        keyToEntry.remove(key);
        valueToEntry.remove(entry.value());
        keyToEntryInfo.remove(key);
        dynreg$freeIds.add(rawId);
    }

    @ModifyExpressionValue(method = "add", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/ObjectList;size()I"))
    private int getNextId(int original) {
        if (!dynreg$freeIds.isEmpty()) {
            int first = dynreg$freeIds.firstInt();
            dynreg$freeIds.remove(first);
            return first;
        }

        return original;
    }

    @WrapOperation(method = "add", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/ObjectList;add(Ljava/lang/Object;)Z"))
    private boolean notAddButSet(ObjectList<Object> instance, Object o, Operation<Boolean> original, @Local int id) {
        if (id == instance.size()) return original.call(instance, o);

        instance.set(id, o);
        return true;
    }

    @Override
    public void dynreg$unfreeze() {
        frozen = false;

        if (dynreg$intrusive)
            this.intrusiveValueToEntry = new IdentityHashMap<>();
    }

    @Inject(method = "freeze", at = @At("HEAD"))
    private void onFreeze(CallbackInfoReturnable<Registry<T>> cir) {
        dynreg$registryFrozenEvent.invoker().onRegistryFrozen();
    }


    @ModifyArg(method = "iterator", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Iterators;transform(Ljava/util/Iterator;Lcom/google/common/base/Function;)Ljava/util/Iterator;"))
    private Iterator<RegistryEntry.Reference<T>> removeNulls(Iterator<RegistryEntry.Reference<T>> iterator) {
        return Iterators.filter(iterator, Objects::nonNull);
    }
}
