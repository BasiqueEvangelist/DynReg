package me.basiqueevangelist.dynreg.entry.item;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.entry.*;
import me.basiqueevangelist.dynreg.entry.json.SimpleReaders;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SimpleItemEntry implements RegistrationEntry {
    private final Identifier id;
    private final Item.Settings settings;

    public SimpleItemEntry(Identifier id, Item.Settings settings) {
        this.id = id;
        this.settings = settings;
    }

    public SimpleItemEntry(Identifier id, JsonObject obj) {
        this.id = id;
        this.settings = SimpleReaders.readItemSettings(obj);
    }

    public SimpleItemEntry(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.settings = SimpleSerializers.readItemSettings(buf);
    }

    @Override
    public void scan(EntryScanContext ctx) {
        ctx.announce(Registry.ITEM, id);
    }

    @Override
    public void register(EntryRegisterContext ctx) {
        ctx.register(Registry.ITEM, id, new Item(settings));
    }

    @Override
    public void write(PacketByteBuf buf) {
        SimpleSerializers.writeItemSettings(buf, settings);
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public int hash() {
        int hash = id.hashCode();
        hash = 31 * hash + SimpleHashers.hash(settings);
        return hash;
    }
}
