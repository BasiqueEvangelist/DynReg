package me.basiqueevangelist.dynreg.testmod.desc;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.api.entry.EntryRegisterContext;
import me.basiqueevangelist.dynreg.api.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.api.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.api.ser.LazyItemSettings;
import me.basiqueevangelist.dynreg.api.ser.SimpleHashers;
import me.basiqueevangelist.dynreg.api.ser.SimpleReaders;
import me.basiqueevangelist.dynreg.testmod.DynRegTest;
import me.basiqueevangelist.dynreg.api.ser.SimpleSerializers;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class SlabBlockEntry implements RegistrationEntry {
    public static final Identifier ID = DynRegTest.id("slab");

    private final Identifier id;
    private final AbstractBlock.Settings blockSettings;
    private final LazyItemSettings itemSettings;

    public SlabBlockEntry(Identifier id, JsonObject json) {
        this.id = id;
        this.blockSettings = SimpleReaders.readBlockSettings(json);
        this.itemSettings = new LazyItemSettings(json);
    }

    public SlabBlockEntry(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.blockSettings = SimpleSerializers.readBlockSettings(buf);
        this.itemSettings = new LazyItemSettings(buf);
    }

    @Override
    public void scan(EntryScanContext ctx) {
        ctx.announce(Registries.BLOCK, id);
        ctx.announce(Registries.ITEM, id);

        itemSettings.scan(ctx);
    }

    @Override
    public void register(EntryRegisterContext ctx) {
        SlabBlock stairs = new SlabBlock(blockSettings);
        ctx.register(Registries.BLOCK, id, stairs);
        BlockItem item = new BlockItem(stairs, itemSettings.build());
        ctx.register(Registries.ITEM, id, item);
    }

    public void write(PacketByteBuf buf) {
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
        hash = 31 * hash + SimpleHashers.hash(blockSettings);
        hash = 31 * hash + itemSettings.hashCode();
        return hash;
    }
}
