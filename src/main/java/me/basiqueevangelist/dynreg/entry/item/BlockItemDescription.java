package me.basiqueevangelist.dynreg.entry.item;

import me.basiqueevangelist.dynreg.network.SimpleSerializers;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

public class BlockItemDescription implements ItemDescription {
    private final Item.Settings settings;
    private final Supplier<Block> blockSupplier;
    private Block block;

    public BlockItemDescription(Block block, Item.Settings settings) {
        this.settings = settings;
        this.block = block;
        this.blockSupplier = null;
    }

    public BlockItemDescription(Supplier<Block> blockSupplier, Item.Settings settings) {
        this.settings = settings;
        this.blockSupplier = blockSupplier;
        this.block = null;
    }

    public BlockItemDescription(PacketByteBuf buf) {
        settings = SimpleSerializers.readItemSettings(buf);
        blockSupplier = null;
        block = Registry.BLOCK.get(buf.readIdentifier());
    }

    private Block getBlock() {
        if (block == null) {
            block = blockSupplier.get();
        }

        return block;
    }

    @Override
    public void write(PacketByteBuf buf) {
        SimpleSerializers.writeItemSettings(buf, settings);
        //noinspection deprecation
        buf.writeIdentifier(getBlock().getRegistryEntry().registryKey().getValue());
    }

    @Override
    public Item create() {
        return new BlockItem(getBlock(), settings);
    }
}
