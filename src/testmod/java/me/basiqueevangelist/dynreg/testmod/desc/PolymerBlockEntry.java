package me.basiqueevangelist.dynreg.testmod.desc;

import com.google.gson.JsonObject;
import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.item.PolymerBlockItem;
import me.basiqueevangelist.dynreg.entry.*;
import me.basiqueevangelist.dynreg.entry.json.SimpleReaders;
import me.basiqueevangelist.dynreg.testmod.DynRegTest;
import me.basiqueevangelist.dynreg.util.LazyEntryRef;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class PolymerBlockEntry implements RegistrationEntry {
    public static final Identifier ID = DynRegTest.id("polymer_block");

    private final Identifier id;
    private final LazyEntryRef<Block> sourceBlock;
    private final AbstractBlock.Settings blockSettings;
    private final Item.Settings itemSettings;

    public PolymerBlockEntry(Identifier id, JsonObject json) {
        this.id = id;
        this.sourceBlock = new LazyEntryRef<>(Registry.BLOCK, new Identifier(JsonHelper.getString(json, "source_block")));
        this.blockSettings = SimpleReaders.readBlockSettings(json);
        this.itemSettings = SimpleReaders.readItemSettings(json);
    }

    public PolymerBlockEntry(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.sourceBlock = LazyEntryRef.read(buf, Registry.BLOCK);
        this.blockSettings = SimpleSerializers.readBlockSettings(buf);
        this.itemSettings = SimpleSerializers.readItemSettings(buf);
    }

    @Override
    public void scan(EntryScanContext ctx) {
        ctx.announce(Registry.BLOCK, id)
            .dependency(sourceBlock);
        ctx.announce(Registry.ITEM, id)
            .dependency(Registry.BLOCK, id);
    }

    @Override
    public void register(EntryRegisterContext ctx) {
        var block = new CreatedBlock(sourceBlock.get(), blockSettings);
        ctx.register(Registry.BLOCK, id, block);
        BlockItem item = new PolymerBlockItem(block, itemSettings, sourceBlock.get().asItem());
        ctx.register(Registry.ITEM, id, item);
    }

    @Override
    public void write(PacketByteBuf buf) {
        sourceBlock.write(buf);
        SimpleSerializers.writeBlockSettings(buf, blockSettings);
        SimpleSerializers.writeItemSettings(buf, itemSettings);
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public int hash() {
        int hash = id.hashCode();
        hash = 31 * hash + sourceBlock.hashCode();
        hash = 31 * hash + SimpleHashers.hash(blockSettings);
        hash = 31 * hash + SimpleHashers.hash(itemSettings);
        return hash;
    }

    @Override
    public @Nullable RegistrationEntry toSynced(PlayerEntity player) {
        return null;
    }

    private static class CreatedBlock extends Block implements PolymerBlock {
        private final Block source;

        public CreatedBlock(Block source, AbstractBlock.Settings settings) {
            super(settings);
            this.source = source;
        }

        @Override
        public Block getPolymerBlock(BlockState state) {
            return source;
        }
    }
}
