package me.basiqueevangelist.dynreg.api.ser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import me.basiqueevangelist.dynreg.api.entry.EntryScanContext;
import net.minecraft.item.FoodComponent;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper of {@link FoodComponent} that uses lazy registry entry resolution
 */
public class LazyFoodComponent {
    private boolean isMeat;
    private boolean isAlwaysEdible;
    private boolean isSnack;
    private int hunger;
    private float saturationModifier;
    private final List<Pair<LazyStatusEffectInstance, Float>> statusEffects;

    public LazyFoodComponent() {
        this.statusEffects = new ArrayList<>();
    }

    public LazyFoodComponent(PacketByteBuf buf) {
        this.hunger = buf.readVarInt();
        this.saturationModifier = buf.readFloat();
        byte flags = buf.readByte();
        this.isMeat = (flags & 1) != 0;
        this.isAlwaysEdible = (flags & 1) != 0;
        this.isSnack = (flags & 1) != 0;
        this.statusEffects = buf.readList(buf2 -> new Pair<>(new LazyStatusEffectInstance(buf2), buf2.readFloat()));
    }

    public LazyFoodComponent(JsonObject obj) {
        this.hunger = JsonHelper.getInt(obj, "hunger");
        this.saturationModifier = JsonHelper.getInt(obj, "saturation_modifier");
        this.isMeat = JsonHelper.getBoolean(obj, "meat", false);
        this.isAlwaysEdible = JsonHelper.getBoolean(obj, "always_edible", false);
        this.isSnack = JsonHelper.getBoolean(obj, "snack", false);
        this.statusEffects = new ArrayList<>();

        var effects = JsonHelper.getArray(obj, "effects", new JsonArray());

        for (JsonElement el : effects) {
            JsonObject effectObj = JsonHelper.asObject(el, "<effect item>");

            float chance = JsonHelper.getFloat(effectObj, "chance");
            var inst = new LazyStatusEffectInstance(effectObj);

            statusEffects.add(new Pair<>(inst, chance));
        }
    }

    public void scan(EntryScanContext ctx) {
        for (var pair : statusEffects) {
            pair.getFirst().scan(ctx);
        }
    }

    public FoodComponent build() {
        FoodComponent.Builder builder = new FoodComponent.Builder();
        builder.hunger(hunger);
        builder.saturationModifier(saturationModifier);
        if (isMeat) builder.meat();
        if (isAlwaysEdible) builder.alwaysEdible();
        if (isSnack) builder.snack();

        for (var pair : statusEffects) {
            builder.statusEffect(pair.getFirst().build(), pair.getSecond());
        }

        return builder.build();
    }

    public void write(PacketByteBuf buf) {
        byte flags = 0;

        if (isMeat) flags |= 1;
        if (isAlwaysEdible) flags |= 2;
        if (isSnack) flags |= 4;

        buf.writeVarInt(hunger);
        buf.writeFloat(saturationModifier);
        buf.writeByte(flags);

        buf.writeCollection(statusEffects, (buf2, pair) -> {
            pair.getFirst().write(buf2);
            buf2.writeFloat(pair.getSecond());
        });
    }

    public boolean isMeat() {
        return isMeat;
    }

    public LazyFoodComponent isMeat(boolean meat) {
        isMeat = meat;
        return this;
    }

    public boolean isAlwaysEdible() {
        return isAlwaysEdible;
    }

    public LazyFoodComponent isAlwaysEdible(boolean alwaysEdible) {
        isAlwaysEdible = alwaysEdible;
        return this;
    }

    public boolean isSnack() {
        return isSnack;
    }

    public LazyFoodComponent isSnack(boolean snack) {
        isSnack = snack;
        return this;
    }

    public int hunger() {
        return hunger;
    }

    public LazyFoodComponent hunger(int hunger) {
        this.hunger = hunger;
        return this;
    }

    public float saturationModifier() {
        return saturationModifier;
    }

    public LazyFoodComponent saturationModifier(float saturationModifier) {
        this.saturationModifier = saturationModifier;
        return this;
    }

    public List<Pair<LazyStatusEffectInstance, Float>> statusEffects() {
        return statusEffects;
    }

    public LazyFoodComponent effect(LazyStatusEffectInstance inst, float chance) {
        statusEffects.add(new Pair<>(inst, chance));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LazyFoodComponent that = (LazyFoodComponent) o;

        if (isMeat != that.isMeat) return false;
        if (isAlwaysEdible != that.isAlwaysEdible) return false;
        if (isSnack != that.isSnack) return false;
        if (hunger != that.hunger) return false;
        if (Float.compare(that.saturationModifier, saturationModifier) != 0) return false;
        return statusEffects.equals(that.statusEffects);
    }

    @Override
    public int hashCode() {
        int result = (isMeat ? 1 : 0);
        result = 31 * result + (isAlwaysEdible ? 1 : 0);
        result = 31 * result + (isSnack ? 1 : 0);
        result = 31 * result + hunger;
        result = 31 * result + (saturationModifier != 0.0f ? Float.floatToIntBits(saturationModifier) : 0);
        result = 31 * result + statusEffects.hashCode();
        return result;
    }
}
