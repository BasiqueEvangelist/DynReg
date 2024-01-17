package me.basiqueevangelist.dynreg.entry;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.api.entry.EntryRegisterContext;
import me.basiqueevangelist.dynreg.api.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.api.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.api.ser.LazyItemSettings;
import me.basiqueevangelist.dynreg.api.ser.SimpleHashers;
import me.basiqueevangelist.dynreg.api.ser.SimpleReaders;
import me.basiqueevangelist.dynreg.api.ser.SimpleSerializers;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class SimpleBlockEntry implements RegistrationEntry {
    private final Identifier id;
    private final AbstractBlock.Settings blockSettings;
    private final LazyItemSettings itemSettings;

    public SimpleBlockEntry(Identifier id, AbstractBlock.Settings blockSettings, LazyItemSettings itemSettings) {
        this.id = id;
        this.blockSettings = blockSettings;
        this.itemSettings = itemSettings;
    }

    public SimpleBlockEntry(Identifier id, JsonObject obj) {
        this.id = id;
        this.blockSettings = SimpleReaders.readBlockSettings(obj);
        this.itemSettings = new LazyItemSettings(obj);
    }

    public SimpleBlockEntry(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.blockSettings = SimpleSerializers.readBlockSettings(buf);

        if (buf.readBoolean()) {
            this.itemSettings = new LazyItemSettings(buf);
        } else {
            this.itemSettings = null;
        }
    }

    @Override
    public void scan(EntryScanContext ctx) {
        ctx.announce(Registries.BLOCK, id);
        ctx.announce(Registries.ITEM, id);

        itemSettings.scan(ctx);
    }

    @Override
    public void register(EntryRegisterContext ctx) {
        var block = ctx.register(Registries.BLOCK, id, new Block(blockSettings));

        ctx.register(Registries.ITEM, id, new BlockItem(block, itemSettings.build()));
    }

    public void write(PacketByteBuf buf) {
        SimpleSerializers.writeBlockSettings(buf, blockSettings);

        if (itemSettings != null) {
            buf.writeBoolean(true);
            itemSettings.write(buf);
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public long hash() {
        int hash = id.hashCode();
        hash = 31 * hash + SimpleHashers.hash(blockSettings);
        hash = 31 * hash + (itemSettings != null ? itemSettings.hashCode() : 0);
        return hash;
    }
}
