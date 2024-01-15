package me.basiqueevangelist.dynreg.testmod.desc;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.api.entry.EntryRegisterContext;
import me.basiqueevangelist.dynreg.api.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.api.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.wrapped.SimpleHashers;
import me.basiqueevangelist.dynreg.wrapped.SimpleReaders;
import me.basiqueevangelist.dynreg.testmod.DynRegTest;
import me.basiqueevangelist.dynreg.util.LazyEntryRef;
import me.basiqueevangelist.dynreg.wrapped.SimpleSerializers;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class FlowerPotBlockEntry implements RegistrationEntry {
    public static final Identifier ID = DynRegTest.id("flower_pot_block");

    private final Identifier id;
    private final LazyEntryRef<Block> content;
    private final AbstractBlock.Settings settings;

    public FlowerPotBlockEntry(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.content = LazyEntryRef.read(buf, Registries.BLOCK);
        this.settings = SimpleSerializers.readBlockSettings(buf);
    }

    public FlowerPotBlockEntry(Identifier id, JsonObject obj) {
        this.id = id;
        this.content = new LazyEntryRef<>(Registries.BLOCK, new Identifier(JsonHelper.getString(obj, "content")));
        this.settings = SimpleReaders.readBlockSettings(obj);
    }

    @Override
    public void scan(EntryScanContext ctx) {
        ctx.announce(Registries.BLOCK, id);
        ctx.dependency(content);
    }

    @Override
    public void register(EntryRegisterContext ctx) {
        ctx.register(Registries.BLOCK, id, new FlowerPotBlock(content.get(), settings));
    }

    @Override
    public void write(PacketByteBuf buf) {
        content.write(buf);
        SimpleSerializers.writeBlockSettings(buf, settings);
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public long hash() {
        long hash = id.hashCode();
        hash = 31 * hash + content.hashCode();
        hash = 31 * hash + SimpleHashers.hash(settings);
        return hash;
    }
}
