package me.basiqueevangelist.dynreg.network.item;

import me.basiqueevangelist.dynreg.network.EntryDescriptions;
import me.basiqueevangelist.dynreg.network.SimpleSerializers;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BlockItemDescription implements ItemDescription {
    private final Item.Settings settings;
    private final Block block;

    public BlockItemDescription(Block block, Item.Settings settings) {
        this.settings = settings;
        this.block = block;
    }

    public BlockItemDescription(PacketByteBuf buf) {
        settings = SimpleSerializers.readItemSettings(buf);
        block = Registry.BLOCK.get(buf.readIdentifier());
    }

    @Override
    public void write(PacketByteBuf buf) {
        SimpleSerializers.writeItemSettings(buf, settings);
        //noinspection deprecation
        buf.writeIdentifier(block.getRegistryEntry().registryKey().getValue());
    }

    @Override
    public Item create() {
        return new BlockItem(block, settings);
    }
}
