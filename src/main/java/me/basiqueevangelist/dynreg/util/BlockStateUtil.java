package me.basiqueevangelist.dynreg.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BlockStateUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/BlockStateUtil");

    private BlockStateUtil() {

    }

    public static BlockState recreateState(BlockState oldState) {
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
