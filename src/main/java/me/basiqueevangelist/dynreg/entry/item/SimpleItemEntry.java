package me.basiqueevangelist.dynreg.entry.item;

import me.basiqueevangelist.dynreg.entry.EntryRegisterContext;
import me.basiqueevangelist.dynreg.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.network.SimpleSerializers;
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
}
