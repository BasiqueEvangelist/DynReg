package me.basiqueevangelist.dynreg.testmod.desc;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.entry.*;
import me.basiqueevangelist.dynreg.wrapped.SimpleHashers;
import me.basiqueevangelist.dynreg.wrapped.SimpleReaders;
import me.basiqueevangelist.dynreg.testmod.DynRegTest;
import me.basiqueevangelist.dynreg.wrapped.SimpleSerializers;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SlabBlockEntry implements RegistrationEntry {
    public static final Identifier ID = DynRegTest.id("slab");

    private final Identifier id;
    private final AbstractBlock.Settings blockSettings;
    private final Item.Settings itemSettings;

    public SlabBlockEntry(Identifier id, JsonObject json) {
        this.id = id;
        this.blockSettings = SimpleReaders.readBlockSettings(json);
        this.itemSettings = SimpleReaders.readItemSettings(json);
    }

    public SlabBlockEntry(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.blockSettings = SimpleSerializers.readBlockSettings(buf);
        this.itemSettings = SimpleSerializers.readItemSettings(buf);
    }

    @Override
    public void scan(EntryScanContext ctx) {
        ctx.announce(Registry.BLOCK, id);
        ctx.announce(Registry.ITEM, id);
    }

    @Override
    public void register(EntryRegisterContext ctx) {
        SlabBlock stairs = new SlabBlock(blockSettings);
        ctx.register(Registry.BLOCK, id, stairs);
        BlockItem item = new BlockItem(stairs, itemSettings);
        ctx.register(Registry.ITEM, id, item);
    }

    @Override
    public void write(PacketByteBuf buf) {
        SimpleSerializers.writeBlockSettings(buf, blockSettings);
        SimpleSerializers.writeItemSettings(buf, itemSettings);
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public int hash() {
        int hash = id.hashCode();
        hash = 31 * hash + SimpleHashers.hash(blockSettings);
        hash = 31 * hash + SimpleHashers.hash(itemSettings);
        return hash;
    }
}
