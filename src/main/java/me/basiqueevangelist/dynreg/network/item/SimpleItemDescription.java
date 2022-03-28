package me.basiqueevangelist.dynreg.network.item;

import me.basiqueevangelist.dynreg.network.EntryDescription;
import me.basiqueevangelist.dynreg.network.EntryDescriptions;
import me.basiqueevangelist.dynreg.network.SimpleSerializers;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SimpleItemDescription implements ItemDescription {
    private final Item.Settings settings;

    public SimpleItemDescription(Item.Settings settings) {
        this.settings = settings;
    }

    public SimpleItemDescription(PacketByteBuf buf) {
        settings = SimpleSerializers.readItemSettings(buf);
    }

    @Override
    public void write(PacketByteBuf buf) {
        SimpleSerializers.writeItemSettings(buf, settings);
    }

    @Override
    public Item create() {
        return new Item(settings);
    }
}
