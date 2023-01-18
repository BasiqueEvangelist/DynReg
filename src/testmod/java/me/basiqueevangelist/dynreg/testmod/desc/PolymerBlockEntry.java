package me.basiqueevangelist.dynreg.testmod.desc;

import com.google.gson.JsonObject;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import me.basiqueevangelist.dynreg.entry.EntryRegisterContext;
import me.basiqueevangelist.dynreg.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.testmod.DynRegTest;
import me.basiqueevangelist.dynreg.util.LazyEntryRef;
import me.basiqueevangelist.dynreg.wrapped.LazyItemSettings;
import me.basiqueevangelist.dynreg.wrapped.SimpleHashers;
import me.basiqueevangelist.dynreg.wrapped.SimpleReaders;
import me.basiqueevangelist.dynreg.wrapped.SimpleSerializers;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class PolymerBlockEntry implements RegistrationEntry {
    public static final Identifier ID = DynRegTest.id("polymer_block");

    private final Identifier id;
    private final LazyEntryRef<Block> sourceBlock;
    private final AbstractBlock.Settings blockSettings;
    private final LazyItemSettings itemSettings;

    public PolymerBlockEntry(Identifier id, JsonObject json) {
        this.id = id;
        this.sourceBlock = new LazyEntryRef<>(Registries.BLOCK, new Identifier(JsonHelper.getString(json, "source_block")));
        this.blockSettings = SimpleReaders.readBlockSettings(json);
        this.itemSettings = new LazyItemSettings(json);
    }

    public PolymerBlockEntry(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.sourceBlock = LazyEntryRef.read(buf, Registries.BLOCK);
        this.blockSettings = SimpleSerializers.readBlockSettings(buf);
        this.itemSettings = new LazyItemSettings(buf);
    }

    @Override
    public void scan(EntryScanContext ctx) {
        ctx.announce(Registries.BLOCK, id);
        ctx.announce(Registries.ITEM, id);

        ctx.dependency(sourceBlock);
        itemSettings.scan(ctx);
    }

    @Override
    public void register(EntryRegisterContext ctx) {
        var block = new CreatedBlock(sourceBlock.get(), blockSettings);
        ctx.register(Registries.BLOCK, id, block);
        BlockItem item = new PolymerBlockItem(block, itemSettings.build(), sourceBlock.get().asItem());
        ctx.register(Registries.ITEM, id, item);
    }

    @Override
    public void write(PacketByteBuf buf) {
        sourceBlock.write(buf);
        SimpleSerializers.writeBlockSettings(buf, blockSettings);
        itemSettings.write(buf);
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
        hash = 31 * hash + itemSettings.hashCode();
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
