package me.basiqueevangelist.dynreg.testmod.desc;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.api.entry.EntryRegisterContext;
import me.basiqueevangelist.dynreg.api.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.api.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.testmod.DynRegTest;
import me.basiqueevangelist.dynreg.util.LazyEntryRef;
import me.basiqueevangelist.dynreg.wrapped.LazyItemSettings;
import me.basiqueevangelist.dynreg.wrapped.SimpleHashers;
import me.basiqueevangelist.dynreg.wrapped.SimpleReaders;
import me.basiqueevangelist.dynreg.wrapped.SimpleSerializers;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.StairsBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class StairsBlockEntry implements RegistrationEntry {
    public static final Identifier ID = DynRegTest.id("stairs");

    private final Identifier id;
    private final LazyEntryRef<Block> sourceBlock;
    private final AbstractBlock.Settings blockSettings;
    private final LazyItemSettings itemSettings;

    public StairsBlockEntry(Identifier id, JsonObject json) {
        this.id = id;
        this.sourceBlock = new LazyEntryRef<>(Registries.BLOCK, new Identifier(JsonHelper.getString(json, "source_block")));
        this.blockSettings = SimpleReaders.readBlockSettings(json);
        this.itemSettings = new LazyItemSettings(json);
    }

    public StairsBlockEntry(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.sourceBlock = LazyEntryRef.read(buf, Registries.BLOCK);
        this.blockSettings = SimpleSerializers.readBlockSettings(buf);
        this.itemSettings = new LazyItemSettings(buf);
    }

    @Override
    public void scan(EntryScanContext ctx) {
        ctx.announce(Registries.BLOCK, id);
        ctx.announce(Registries.ITEM, id);

        ctx.dependency(sourceBlock);
        itemSettings.scan(ctx);
    }

    @Override
    public void register(EntryRegisterContext ctx) {
        StairsBlock stairs = new StairsBlock(sourceBlock.get().getDefaultState(), blockSettings);
        ctx.register(Registries.BLOCK, id, stairs);
        BlockItem item = new BlockItem(stairs, itemSettings.build());
        ctx.register(Registries.ITEM, id, item);
    }

    @Override
    public void write(PacketByteBuf buf) {
        sourceBlock.write(buf);
        SimpleSerializers.writeBlockSettings(buf, blockSettings);
        itemSettings.write(buf);
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public long hash() {
        int hash = id.hashCode();
        hash = 31 * hash + sourceBlock.hashCode();
        hash = 31 * hash + SimpleHashers.hash(blockSettings);
        hash = 31 * hash + itemSettings.hashCode();
        return hash;
    }
}
