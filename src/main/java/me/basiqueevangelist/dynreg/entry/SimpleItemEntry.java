package me.basiqueevangelist.dynreg.entry;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.api.entry.EntryRegisterContext;
import me.basiqueevangelist.dynreg.api.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.api.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.api.ser.LazyItemSettings;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class SimpleItemEntry implements RegistrationEntry {
    private final Identifier id;
    private final LazyItemSettings settings;

    public SimpleItemEntry(Identifier id, LazyItemSettings settings) {
        this.id = id;
        this.settings = settings;
    }

    public SimpleItemEntry(Identifier id, JsonObject obj) {
        this.id = id;
        this.settings = new LazyItemSettings(obj);
    }

    public SimpleItemEntry(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.settings = new LazyItemSettings(buf);
    }

    @Override
    public void scan(EntryScanContext ctx) {
        ctx.announce(Registries.ITEM, id);

        settings.scan(ctx);
    }

    @Override
    public void register(EntryRegisterContext ctx) {
        ctx.register(Registries.ITEM, id, new Item(settings.build()));
    }

    public void write(PacketByteBuf buf) {
        settings.write(buf);
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public long hash() {
        int hash = id.hashCode();
        hash = 31 * hash + settings.hashCode();
        return hash;
    }
}
