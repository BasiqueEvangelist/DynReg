package me.basiqueevangelist.dynreg.testmod.desc;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.entry.*;
import me.basiqueevangelist.dynreg.entry.json.SimpleReaders;
import me.basiqueevangelist.dynreg.testmod.DynRegTest;
import me.basiqueevangelist.dynreg.util.LazyEntryRef;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.StairsBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class StairsBlockDescription implements RegistrationEntry {
    public static final Identifier ID = DynRegTest.id("stairs");

    private final Identifier id;
    private final LazyEntryRef<Block> sourceBlock;
    private final AbstractBlock.Settings blockSettings;
    private final Item.Settings itemSettings;

    public StairsBlockDescription(Identifier id, JsonObject json) {
        this.id = id;
        this.sourceBlock = new LazyEntryRef<>(Registry.BLOCK, new Identifier(JsonHelper.getString(json, "source_block")));
        this.blockSettings = SimpleReaders.readBlockSettings(json);
        this.itemSettings = SimpleReaders.readItemSettings(json);
    }

    public StairsBlockDescription(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.sourceBlock = LazyEntryRef.read(buf, Registry.BLOCK);
        this.blockSettings = SimpleSerializers.readBlockSettings(buf);
        this.itemSettings = SimpleSerializers.readItemSettings(buf);
    }

    @Override
    public void scan(EntryScanContext ctx) {
        ctx.announce(Registry.BLOCK, id)
            .dependency(sourceBlock);
        ctx.announce(Registry.ITEM, id)
            .dependency(Registry.BLOCK, id);
    }

    @Override
    public void register(EntryRegisterContext ctx) {
        StairsBlock stairs = new StairsBlock(sourceBlock.get().getDefaultState(), blockSettings);
        ctx.register(Registry.BLOCK, id, stairs);
        BlockItem item = new BlockItem(stairs, itemSettings);
        ctx.register(Registry.ITEM, id, item);
    }

    @Override
    public void write(PacketByteBuf buf) {
        sourceBlock.write(buf);
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
        hash = 31 * hash + sourceBlock.hashCode();
        hash = 31 * hash + SimpleHashers.hash(blockSettings);
        hash = 31 * hash + SimpleHashers.hash(itemSettings);
        return hash;
    }
}
