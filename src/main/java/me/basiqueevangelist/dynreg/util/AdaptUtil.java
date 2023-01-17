package me.basiqueevangelist.dynreg.util;

import com.mojang.datafixers.util.Pair;
import me.basiqueevangelist.dynreg.access.DeletableObjectInternal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public final class AdaptUtil {
    private AdaptUtil() {

    }

    public static @Nullable StatusEffect adaptEffect(StatusEffect effect) {
        return Registry.STATUS_EFFECT
            .getOrEmpty(((DeletableObjectInternal) effect).dynreg$getId())
            .orElse(null);
    }

    public static BlockState adaptState(BlockState oldState) {
        @SuppressWarnings("deprecation") Identifier id = oldState.getBlock().getRegistryEntry().registryKey().getValue();

        Block newBlock = Registry.BLOCK.getOrEmpty(id).orElse(null);

        if (newBlock == null) {
            return Blocks.AIR.getDefaultState();
        }

        BlockState newState = newBlock.getDefaultState();

        for (Property<?> property : oldState.getProperties()) {
            newState = tryTransferProperty(oldState, newBlock, newState, property);
        }

        return newState;
    }

    private static <T extends Comparable<T>> BlockState tryTransferProperty(BlockState oldState, Block newBlock, BlockState newState, Property<T> property) {
        Property<?> newProperty = newBlock.getStateManager().getProperty(property.getName());

        if (newProperty == null) return newState;

        var tag = property
            .getValueCodec()
            .encodeStart(NbtOps.INSTANCE, property.createValue(oldState))
            .get()
            .left()
            .orElse(null);

        if (tag == null) return newState;

        return tryDecodeAndSet(newState, tag, newProperty);
    }

    private static <T extends Comparable<T>> BlockState tryDecodeAndSet(BlockState newState, NbtElement tag, Property<T> newProperty) {
        var val = newProperty
            .getValueCodec()
            .decode(NbtOps.INSTANCE, tag)
            .get()
            .left()
            .map(Pair::getFirst)
            .orElse(null);

        if (val == null) return newState;

        return newState.with(newProperty, val.value());
    }
}
