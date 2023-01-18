package me.basiqueevangelist.dynreg.entry;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.wrapped.SimpleHashers;
import me.basiqueevangelist.dynreg.wrapped.SimpleReaders;
import me.basiqueevangelist.dynreg.wrapped.SimpleSerializers;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class SimpleBlockEntry implements RegistrationEntry {
    private final Identifier id;
    private final AbstractBlock.Settings blockSettings;
    private final Item.Settings itemSettings;

    public SimpleBlockEntry(Identifier id, AbstractBlock.Settings blockSettings, Item.Settings itemSettings) {
        this.id = id;
        this.blockSettings = blockSettings;
        this.itemSettings = itemSettings;
    }

    public SimpleBlockEntry(Identifier id, JsonObject obj) {
        this.id = id;
        this.blockSettings = SimpleReaders.readBlockSettings(obj);
        this.itemSettings = SimpleReaders.readItemSettings(obj);
    }

    public SimpleBlockEntry(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.blockSettings = SimpleSerializers.readBlockSettings(buf);

        if (buf.readBoolean()) {
            this.itemSettings = SimpleSerializers.readItemSettings(buf);
        } else {
            this.itemSettings = null;
        }
    }

    @Override
    public void scan(EntryScanContext ctx) {
        ctx.announce(Registries.BLOCK, id);
        ctx.announce(Registries.ITEM, id);
    }

    @Override
    public void register(EntryRegisterContext ctx) {
        var block = ctx.register(Registries.BLOCK, id, new Block(blockSettings));

        ctx.register(Registries.ITEM, id, new BlockItem(block, itemSettings));
    }

    @Override
    public void write(PacketByteBuf buf) {
        SimpleSerializers.writeBlockSettings(buf, blockSettings);

        if (itemSettings != null) {
            buf.writeBoolean(true);
            SimpleSerializers.writeItemSettings(buf, itemSettings);
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public int hash() {
        int hash = id.hashCode();
        hash = 31 * hash + SimpleHashers.hash(blockSettings);
        hash = 31 * hash + (itemSettings != null ? SimpleHashers.hash(itemSettings) : 0);
        return hash;
    }
}
