package me.basiqueevangelist.dynreg.network.block;

import me.basiqueevangelist.dynreg.network.EntryDescriptions;
import me.basiqueevangelist.dynreg.network.SimpleSerializers;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SimpleBlockDescription implements BlockDescription {
    private final AbstractBlock.Settings settings;

    public SimpleBlockDescription(AbstractBlock.Settings settings) {
        this.settings = settings;
    }

    public SimpleBlockDescription(PacketByteBuf buf) {
        settings = SimpleSerializers.readBlockSettings(buf);
    }

    @Override
    public Block create() {
        return new Block(settings);
    }

    @Override
    public void write(PacketByteBuf buf) {
        SimpleSerializers.writeBlockSettings(buf, settings);
    }
}
