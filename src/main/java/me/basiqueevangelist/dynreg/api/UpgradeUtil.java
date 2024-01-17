package me.basiqueevangelist.dynreg.api;

import com.mojang.datafixers.util.Pair;
import me.basiqueevangelist.dynreg.access.DeletableObjectInternal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Utilities for upgrading removed entries to their newer versions.
 */
public final class UpgradeUtil {
    private UpgradeUtil() {

    }

    /**
     * Tries to upgrade {@code effect}.
     *
     * @return the new version of the effect or {@code null} if there is none
     */
    public static @Nullable StatusEffect upgradeEffect(StatusEffect effect) {
        return Registries.STATUS_EFFECT
            .getOrEmpty(((DeletableObjectInternal) effect).dynreg$getId())
            .orElse(null);
    }

    /**
     * Tries to upgrade {@code oldState}.
     *
     * @return the new version of the state or {@link Blocks#AIR}'s default state if there is none
     */
    public static BlockState upgradeBlockState(BlockState oldState) {
        @SuppressWarnings("deprecation") Identifier id = oldState.getBlock().getRegistryEntry().registryKey().getValue();

        Block newBlock = Registries.BLOCK.getOrEmpty(id).orElse(null);

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
