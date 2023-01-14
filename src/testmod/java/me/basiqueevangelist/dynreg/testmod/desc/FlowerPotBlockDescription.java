package me.basiqueevangelist.dynreg.testmod.desc;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.entry.*;
import me.basiqueevangelist.dynreg.entry.json.SimpleReaders;
import me.basiqueevangelist.dynreg.testmod.DynRegTest;
import me.basiqueevangelist.dynreg.util.LazyEntryRef;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class FlowerPotBlockDescription implements RegistrationEntry {
    public static final Identifier ID = DynRegTest.id("flower_pot_block");

    private final Identifier id;
    private final LazyEntryRef<Block> content;
    private final AbstractBlock.Settings settings;

    public FlowerPotBlockDescription(Identifier id, Identifier contentId, AbstractBlock.Settings settings) {
        this.id = id;
        this.settings = settings;
        this.content = new LazyEntryRef<>(Registry.BLOCK, contentId);
    }

    public FlowerPotBlockDescription(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.content = LazyEntryRef.read(buf, Registry.BLOCK);
        this.settings = SimpleSerializers.readBlockSettings(buf);
    }

    public FlowerPotBlockDescription(Identifier id, JsonObject obj) {
        this.id = id;
        this.content = new LazyEntryRef<>(Registry.BLOCK, new Identifier(JsonHelper.getString(obj, "content")));
        this.settings = SimpleReaders.readBlockSettings(obj);
    }

    @Override
    public void scan(EntryScanContext ctx) {
        ctx.announce(Registry.BLOCK, id)
            .dependency(content);
    }

    @Override
    public void register(EntryRegisterContext ctx) {
        ctx.register(Registry.BLOCK, id, new FlowerPotBlock(content.get(), settings));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(content.id());
        SimpleSerializers.writeBlockSettings(buf, settings);
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public int hash() {
        int hash = id.hashCode();
        hash = 31 * hash + content.hashCode();
        hash = 31 * hash + SimpleHashers.hash(settings);
        return hash;
    }
}
