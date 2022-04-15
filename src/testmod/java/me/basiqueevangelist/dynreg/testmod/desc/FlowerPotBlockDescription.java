package me.basiqueevangelist.dynreg.testmod.desc;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.entry.block.BlockDescription;
import me.basiqueevangelist.dynreg.entry.item.BlockItemDescription;
import me.basiqueevangelist.dynreg.entry.json.SimpleReaders;
import me.basiqueevangelist.dynreg.network.SimpleSerializers;
import me.basiqueevangelist.dynreg.testmod.DynRegTest;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

public class FlowerPotBlockDescription implements BlockDescription {
    public static final Identifier ID = DynRegTest.id("flower_pot_block");

    private final AbstractBlock.Settings settings;
    private final Supplier<Block> contentSupplier;
    private Block content;

    public FlowerPotBlockDescription(Block content, AbstractBlock.Settings settings) {
        this.settings = settings;
        this.content = content;
        this.contentSupplier = null;
    }

    public FlowerPotBlockDescription(Supplier<Block> contentSupplier, AbstractBlock.Settings settings) {
        this.settings = settings;
        this.contentSupplier = contentSupplier;
        this.content = null;
    }

    public FlowerPotBlockDescription(PacketByteBuf buf) {
        settings = SimpleSerializers.readBlockSettings(buf);
        contentSupplier = null;
        content = Registry.BLOCK.get(buf.readIdentifier());
    }

    public FlowerPotBlockDescription(JsonObject obj) {
        Identifier blockId = new Identifier(JsonHelper.getString(obj, "content"));
        this.contentSupplier = () -> Registry.BLOCK.get(blockId);
        this.settings = SimpleReaders.readBlockSettings(obj);
        this.content = null;
    }

    private Block getContent() {
        if (content == null) {
            content = contentSupplier.get();
        }

        return content;
    }

    @Override
    public void write(PacketByteBuf buf) {
        SimpleSerializers.writeBlockSettings(buf, settings);
        //noinspection deprecation
        buf.writeIdentifier(getContent().getRegistryEntry().registryKey().getValue());
    }

    @Override
    public Block create() {
        return new FlowerPotBlock(getContent(), settings);
    }
}
