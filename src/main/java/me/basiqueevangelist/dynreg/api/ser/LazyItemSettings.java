package me.basiqueevangelist.dynreg.api.ser;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.api.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.impl.util.NamedEntries;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
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
    private int maxCount = 64;
    private int maxDamage = 0;
    private @Nullable LazyEntryRef<Item> recipeRemainder = null;
    private Rarity rarity = Rarity.COMMON;
    private @Nullable LazyFoodComponent foodComponent = null;
    private boolean fireproof;

    public LazyItemSettings() {

    }

    public LazyItemSettings(PacketByteBuf buf) {
        this.maxCount = buf.readVarInt();
        this.maxDamage = buf.readVarInt();
        this.recipeRemainder = buf.readNullable((buf1) -> LazyEntryRef.read(buf1, Registries.ITEM));
        this.rarity = NamedEntries.RARITIES.get(buf.readString());
        this.foodComponent = buf.readNullable(LazyFoodComponent::new);
        this.fireproof = buf.readBoolean();
    }

    public LazyItemSettings(JsonObject obj) {
        if (obj.has("max_count"))
            this.maxCount = JsonHelper.getInt(obj, "max_count");

        if (obj.has("max_damage"))
            this.maxDamage = JsonHelper.getInt(obj, "max_damage");

        if (obj.has("recipe_remainder"))
            this.recipeRemainder = new LazyEntryRef<>(Registries.ITEM, new Identifier(JsonHelper.getString(obj, "recipe_remainder")));

        if (obj.has("rarity"))
            this.rarity = NamedEntries.RARITIES.get(JsonHelper.getString(obj, "rarity"));

        if (obj.has("food_component"))
            this.foodComponent = new LazyFoodComponent(JsonHelper.getObject(obj, "food_component"));

        this.fireproof = JsonHelper.getBoolean(obj, "fireproof", false);
    }

    public int maxCount() {
        return maxCount;
    }

    public void maxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public int maxDamage() {
        return maxDamage;
    }

    public void maxDamage(int maxDamage) {
        this.maxDamage = maxDamage;
        this.maxCount = 1;
    }

    public LazyEntryRef<Item> recipeRemainder() {
        return recipeRemainder;
    }

    public void recipeRemainder(LazyEntryRef<Item> recipeRemainder) {
        this.recipeRemainder = recipeRemainder;
    }

    public Rarity rarity() {
        return rarity;
    }

    public void rarity(Rarity rarity) {
        this.rarity = rarity;
    }

    public LazyFoodComponent foodComponent() {
        return foodComponent;
    }

    public void food(LazyFoodComponent foodComponent) {
        this.foodComponent = foodComponent;
    }

    public boolean fireproof() {
        return fireproof;
    }

    public void fireproof(boolean fireproof) {
        this.fireproof = fireproof;
    }

    public void write(PacketByteBuf buf) {
        buf.writeVarInt(maxCount);
        buf.writeVarInt(maxDamage);
        buf.writeNullable(recipeRemainder, (buf1, itemLazyEntryRef) -> itemLazyEntryRef.write(buf1));
        buf.writeString(NamedEntries.RARITIES.inverse().get(rarity));
        buf.writeNullable(foodComponent, (buf1, lazyFoodComponent) -> foodComponent.write(buf1));
        buf.writeBoolean(fireproof);
    }

    public void scan(EntryScanContext ctx) {
        if (recipeRemainder != null) ctx.dependency(recipeRemainder);

        if (foodComponent != null) foodComponent.scan(ctx);
    }

    public Item.Settings build() {
        Item.Settings settings = new Item.Settings();

        settings.maxCount(maxCount);

        if (this.maxDamage != 0) settings.maxDamage(maxDamage);

        if (recipeRemainder != null) settings.recipeRemainder(recipeRemainder.get());

        settings.rarity(rarity);

        if (foodComponent != null) settings.food(foodComponent.build());

        if (fireproof)
            settings.fireproof();

        return settings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LazyItemSettings that = (LazyItemSettings) o;

        if (maxCount != that.maxCount) return false;
        if (maxDamage != that.maxDamage) return false;
        if (fireproof != that.fireproof) return false;
        if (!Objects.equals(recipeRemainder, that.recipeRemainder))
            return false;
        if (rarity != that.rarity) return false;
        return Objects.equals(foodComponent, that.foodComponent);
    }

    @Override
    public int hashCode() {
        int result = maxCount;
        result = 31 * result + maxDamage;
        result = 31 * result + (recipeRemainder != null ? recipeRemainder.hashCode() : 0);
        result = 31 * result + rarity.hashCode();
        result = 31 * result + (foodComponent != null ? foodComponent.hashCode() : 0);
        result = 31 * result + (fireproof ? 1 : 0);
        return result;
    }
}
