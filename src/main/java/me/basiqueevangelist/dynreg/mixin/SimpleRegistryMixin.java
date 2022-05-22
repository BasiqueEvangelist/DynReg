package me.basiqueevangelist.dynreg.mixin;

import com.google.common.collect.BiMap;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.basiqueevangelist.dynreg.access.DeletableObjectInternal;
import me.basiqueevangelist.dynreg.access.ExtendedRegistry;
import me.basiqueevangelist.dynreg.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.event.RegistryFrozenCallback;
import me.basiqueevangelist.dynreg.util.StackTracingMap;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.registry.RegistryEntryRemovedCallback;
import net.fabricmc.fabric.mixin.registry.sync.MixinIdRegistry;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Mixin(value = SimpleRegistry.class, priority = 1100)
public abstract class SimpleRegistryMixin<T> extends Registry<T> implements ExtendedRegistry<T> {
    @Shadow private boolean frozen;
    @Shadow @Final @Nullable private Function<T, RegistryEntry.Reference<T>> valueToEntryFunction;
    @Shadow @Nullable private Map<T, RegistryEntry.Reference<T>> unfrozenValueToEntry;

    protected SimpleRegistryMixin(RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle) {
        super(key, lifecycle);
    }

    @Shadow public abstract Optional<RegistryEntry<T>> getEntry(RegistryKey<T> key);

    @Shadow @Final private Object2IntMap<T> entryToRawId;
    @Shadow @Final private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;
    @Mutable
    @Shadow @Final private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;
    @Shadow @Final private Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry;
    @Shadow @Final private Map<T, RegistryEntry.Reference<T>> valueToEntry;
    @Shadow @Final private Map<T, Lifecycle> entryToLifecycle;
    @Shadow private volatile Map<TagKey<T>, RegistryEntryList.Named<T>> tagToEntryList;
    @Shadow @Nullable private List<RegistryEntry.Reference<T>> cachedEntries;
    @Shadow private int nextId;

    @SuppressWarnings("ReferenceToMixin")
    @Shadow @Dynamic(mixin = MixinIdRegistry.class) private Object2IntMap<Identifier> fabric_prevIndexedEntries;
    @SuppressWarnings("ReferenceToMixin")
    @Shadow @Dynamic(mixin = MixinIdRegistry.class) private BiMap<Identifier, RegistryEntry.Reference<T>> fabric_prevEntries;

    @SuppressWarnings("unchecked") private final Event<RegistryEntryDeletedCallback<T>> dynreg$entryDeletedEvent = EventFactory.createArrayBacked(RegistryEntryDeletedCallback.class, callbacks -> (rawId, entry) -> {
        for (var callback : callbacks) {
            callback.onEntryDeleted(rawId, entry);
        }

        if (entry.value() instanceof RegistryEntryDeletedCallback<?> callback)
            ((RegistryEntryDeletedCallback<T>)callback).onEntryDeleted(rawId, entry);
    });
    private final Event<RegistryFrozenCallback<T>> dynreg$registryFrozenEvent = EventFactory.createArrayBacked(RegistryFrozenCallback.class, callbacks -> () -> {
        for (var callback : callbacks) {
            callback.onRegistryFrozen();
        }
    });
    private final IntList dynreg$freeIds = new IntArrayList();

    @Override
    public Event<RegistryEntryDeletedCallback<T>> dynreg$getEntryDeletedEvent() {
        return dynreg$entryDeletedEvent;
    }

    @Override
    public Event<RegistryFrozenCallback<T>> dynreg$getRegistryFrozenEvent() {
        return dynreg$registryFrozenEvent;
    }

    @Override
    public void dynreg$remove(RegistryKey<T> key) {
        if (frozen) {
            throw new IllegalStateException("Registry is frozen (trying to remove key " + key + ")");
        }

        RegistryEntry.Reference<T> entry = (RegistryEntry.Reference<T>) getEntry(key).orElseThrow();

        ((DeletableObjectInternal) entry).dynreg$setDeleted(true);

        if (entry.value() instanceof DeletableObjectInternal obj)
            obj.dynreg$setDeleted(true);

        int rawId = entryToRawId.getInt(entry.value());
        dynreg$entryDeletedEvent.invoker().onEntryDeleted(rawId, entry);
        RegistryEntryRemovedCallback.event(this).invoker().onEntryRemoved(rawId, entry.registryKey().getValue(), entry.value());

        rawIdToEntry.set(rawId, null);
        entryToRawId.removeInt(entry.value());
        idToEntry.remove(key.getValue());
        keyToEntry.remove(key);
        valueToEntry.remove(entry.value());
        entryToLifecycle.remove(entry.value());
        dynreg$freeIds.add(rawId);
        fabric_prevEntries.remove(key.getValue());
        fabric_prevIndexedEntries.removeInt(key.getValue());

        cachedEntries = null;
    }

    @Redirect(method = "add", at = @At(value = "FIELD", target = "Lnet/minecraft/util/registry/SimpleRegistry;nextId:I"))
    private int getNextId(SimpleRegistry<T> instance) {
        if (!dynreg$freeIds.isEmpty())
            return dynreg$freeIds.removeInt(0);

        return nextId;
    }

    @Override
    public void dynreg$unfreeze() {
        frozen = false;

        if (valueToEntryFunction != null)
            this.unfrozenValueToEntry = new IdentityHashMap<>();

        cachedEntries = null;
    }

    @Inject(method = "freeze", at = @At("HEAD"))
    private void onFreeze(CallbackInfoReturnable<Registry<T>> cir) {
        dynreg$registryFrozenEvent.invoker().onRegistryFrozen();
    }

    @Override
    public void dynreg$installStackTracingMap() {
        this.idToEntry = new StackTracingMap<>(this.idToEntry);
    }
}
