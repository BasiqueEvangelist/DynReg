package me.basiqueevangelist.dynreg.entry.block;

import me.basiqueevangelist.dynreg.entry.EntryRegisterContext;
import me.basiqueevangelist.dynreg.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.network.SimpleSerializers;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SimpleBlockEntry implements RegistrationEntry {
    private final Identifier id;
    private final AbstractBlock.Settings blockSettings;
    private final Item.Settings itemSettings;

    public SimpleBlockEntry(Identifier id, AbstractBlock.Settings blockSettings, Item.Settings itemSettings) {
        this.id = id;
        this.blockSettings = blockSettings;
        this.itemSettings = itemSettings;
    }

    public SimpleBlockEntry(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.blockSettings = SimpleSerializers.readBlockSettings(buf);

        if (buf.readBoolean()) {
            this.itemSettings = SimpleSerializers.readItemSettings(buf);
        } else {
            this.itemSettings = null;
        }
    }

    @Override
    public void scan(EntryScanContext ctx) {
        ctx.announce(Registry.BLOCK, id);

        ctx.announce(Registry.ITEM, id)
            .dependency(Registry.BLOCK, id);
    }

    @Override
    public void register(EntryRegisterContext ctx) {
        var block = ctx.register(Registry.BLOCK, id, new Block(blockSettings));

        ctx.register(Registry.ITEM, id, new BlockItem(block, itemSettings));
    }

    @Override
    public void write(PacketByteBuf buf) {
        SimpleSerializers.writeBlockSettings(buf, blockSettings);

        if (itemSettings != null) {
            buf.writeBoolean(true);
            SimpleSerializers.writeItemSettings(buf, itemSettings);
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public Identifier id() {
        return id;
    }
}
