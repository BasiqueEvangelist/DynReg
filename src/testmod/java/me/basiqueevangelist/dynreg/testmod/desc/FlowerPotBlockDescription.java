package me.basiqueevangelist.dynreg.testmod.desc;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.entry.EntryRegisterContext;
import me.basiqueevangelist.dynreg.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.entry.json.SimpleReaders;
import me.basiqueevangelist.dynreg.network.SimpleSerializers;
import me.basiqueevangelist.dynreg.testmod.DynRegTest;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class FlowerPotBlockDescription implements RegistrationEntry {
    public static final Identifier ID = DynRegTest.id("flower_pot_block");

    private final Identifier id;
    private final Identifier contentId;
    private final AbstractBlock.Settings settings;

    public FlowerPotBlockDescription(Identifier id, Identifier contentId, AbstractBlock.Settings settings) {
        this.id = id;
        this.settings = settings;
        this.contentId = contentId;
    }

    public FlowerPotBlockDescription(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.contentId = buf.readIdentifier();
        this.settings = SimpleSerializers.readBlockSettings(buf);
    }

    public FlowerPotBlockDescription(Identifier id, JsonObject obj) {
        this.id = id;
        this.contentId = new Identifier(JsonHelper.getString(obj, "content"));
        this.settings = SimpleReaders.readBlockSettings(obj);
    }

    @Override
    public void scan(EntryScanContext ctx) {
        ctx.announce(Registry.BLOCK, id)
            .dependency(Registry.BLOCK, contentId);
    }

    @Override
    public void register(EntryRegisterContext ctx) {
        ctx.register(Registry.BLOCK, id, new FlowerPotBlock(Registry.BLOCK.get(contentId), settings));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(contentId);
        SimpleSerializers.writeBlockSettings(buf, settings);
    }

    @Override
    public Identifier id() {
        return id;
    }
}
