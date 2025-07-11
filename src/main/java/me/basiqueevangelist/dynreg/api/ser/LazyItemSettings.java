package me.basiqueevangelist.dynreg.api.ser;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import me.basiqueevangelist.dynreg.api.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.impl.util.NamedEntries;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Rarity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A wrapper of {@link Item.Settings} that uses lazy registry entry resolution
 */
public class LazyItemSettings {
    private @Nullable LazyEntryRef<Item> recipeRemainder = null;
    private ComponentMap components = ComponentMap.EMPTY;

    private static final PacketCodec<ByteBuf, ComponentMap> COMPONENT_MAP_PACKET_CODEC = PacketCodecs.codec(
        ComponentMap.CODEC
    );

    public LazyItemSettings() {

    }

    public LazyItemSettings(PacketByteBuf buf) {
        this.recipeRemainder = buf.readNullable((buf1) -> LazyEntryRef.read(buf1, Registries.ITEM));
        this.components = COMPONENT_MAP_PACKET_CODEC.decode(buf);
    }

    public LazyItemSettings(JsonObject obj) {
        if (obj.has("recipe_remainder"))
            this.recipeRemainder = new LazyEntryRef<>(Registries.ITEM, Identifier.of(JsonHelper.getString(obj, "recipe_remainder")));

        if (obj.has("components")) {
            this.components = ComponentMap.CODEC.parse(JsonOps.INSTANCE, JsonHelper.getObject(obj, "components")).getOrThrow();
        }
    }

    public LazyEntryRef<Item> recipeRemainder() {
        return recipeRemainder;
    }

    public void recipeRemainder(LazyEntryRef<Item> recipeRemainder) {
        this.recipeRemainder = recipeRemainder;
    }

    public ComponentMap components() {
        return components;
    }

    public void components(ComponentMap components) {
        this.components = components;
    }

    public void write(PacketByteBuf buf) {
        buf.writeNullable(recipeRemainder, (buf1, itemLazyEntryRef) -> itemLazyEntryRef.write(buf1));
        COMPONENT_MAP_PACKET_CODEC.encode(buf, components);
    }

    public void scan(EntryScanContext ctx) {
        if (recipeRemainder != null) ctx.dependency(recipeRemainder);
    }

    @SuppressWarnings("unchecked")
    public Item.Settings build() {
        Item.Settings settings = new Item.Settings();

        if (recipeRemainder != null) settings.recipeRemainder(recipeRemainder.get());

        for (var component : components) {
            settings.component((ComponentType<Object>) component.type(), component.value());
        }

        return settings;
    }

}
