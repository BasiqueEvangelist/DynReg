package me.basiqueevangelist.dynreg.entry.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import me.basiqueevangelist.dynreg.util.NamedEntries;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.*;

public final class SimpleReaders {
    private SimpleReaders() {

    }

    public static Item.Settings readItemSettings(JsonObject obj) {
        Item.Settings builder = new Item.Settings();

        if (obj.has("max_count"))
            builder.maxCount(JsonHelper.getInt(obj, "max_count"));

        if (obj.has("max_damage"))
            builder.maxDamage(JsonHelper.getInt(obj, "max_damage"));

        if (obj.has("recipe_remainder"))
            builder.recipeRemainder(JsonHelper.getItem(obj, "recipe_remainder"));

        if (obj.has("item_group"))
            builder.group(readItemGroup(obj, "item_group"));

        if (obj.has("rarity"))
            builder.rarity(NamedEntries.RARITIES.get(JsonHelper.getString(obj, "rarity")));

        if (obj.has("food_component"))
            builder.food(readFoodComponent(JsonHelper.getObject(obj, "food_component")));

        if (JsonHelper.getBoolean(obj, "fireproof", false))
            builder.fireproof();

        return builder;
    }

    public static AbstractBlock.Settings readBlockSettings(JsonObject obj) {
        Material material;

        if (obj.has("material")) {
            material = readMaterial(obj.get("material"));
        } else {
            throw new JsonSyntaxException("Missing material, expected to be object or string");
        }

        MapColor color = material.getColor();

        if (obj.has("color"))
            color = NamedEntries.MAP_COLORS.get(JsonHelper.getString(obj, "color").toUpperCase(Locale.ROOT));

        FabricBlockSettings settings = FabricBlockSettings.of(material, color);

        if (obj.has("collidable"))
            settings.collidable(JsonHelper.getBoolean(obj, "collidable"));

        if (obj.has("sounds"))
            settings.sounds(readBlockSoundGroup(obj.get("sounds")));

        if (obj.has("resistance"))
            settings.resistance(JsonHelper.getFloat(obj, "resistance"));

        if (obj.has("hardness"))
            settings.hardness(JsonHelper.getFloat(obj, "hardness"));

        if (JsonHelper.getBoolean(obj, "requires_tool", false))
            settings.requiresTool();

        if (JsonHelper.getBoolean(obj, "ticks_randomly", false))
            settings.ticksRandomly();

        if (obj.has("slipperiness"))
            settings.slipperiness(JsonHelper.getFloat(obj, "slipperiness"));

        if (obj.has("velocity_multiplier"))
            settings.velocityMultiplier(JsonHelper.getFloat(obj, "velocity_multiplier"));

        if (obj.has("jump_velocity_multiplier"))
            settings.jumpVelocityMultiplier(JsonHelper.getFloat(obj, "jump_velocity_multiplier"));

        if (obj.has("drops_like"))
            settings.drops(new Identifier(JsonHelper.getString(obj, "drops_like")));

        if (JsonHelper.getBoolean(obj, "non_opaque", false))
            settings.nonOpaque();

        if (JsonHelper.getBoolean(obj, "air", false))
            settings.air();

        if (JsonHelper.getBoolean(obj, "dynamic_bounds", false))
            settings.dynamicBounds();

        return settings;
    }

    public static Material readMaterial(JsonElement el) {
        if (el instanceof JsonPrimitive prim && prim.isString()) {
            return NamedEntries.MATERIALS.get(prim.getAsString().toUpperCase(Locale.ROOT));
        } else if (el instanceof JsonObject obj) {
            boolean isLiquid = JsonHelper.getBoolean(obj, "liquid", false);
            boolean isSolid = JsonHelper.getBoolean(obj, "solid", true);
            boolean blocksMovement = JsonHelper.getBoolean(obj, "blocks_movement", true);
            boolean isBurnable = JsonHelper.getBoolean(obj, "burnable", true);
            boolean isReplaceable = JsonHelper.getBoolean(obj, "replaceable", false);
            boolean blocksLight = JsonHelper.getBoolean(obj, "blocks_light", true);
            PistonBehavior pistonBehaviour = PistonBehavior.valueOf(JsonHelper.getString(obj, "piston_behavior", "normal").toUpperCase(Locale.ROOT));
            MapColor color = NamedEntries.MAP_COLORS.get(JsonHelper.getString(obj, "color").toUpperCase(Locale.ROOT));

            var builder = new Material.Builder(color);

            if (isLiquid) builder.liquid();
            if (!isSolid) builder.notSolid();
            if (!blocksMovement) builder.allowsMovement();
            if (isBurnable) builder.burnable();
            if (isReplaceable) builder.replaceable();
            if (blocksLight) builder.lightPassesThrough();
            builder.pistonBehavior = pistonBehaviour;

            return builder.build();
        } else {
            throw new JsonSyntaxException("Expected material to be object or string");
        }
    }

    public static BlockSoundGroup readBlockSoundGroup(JsonElement el) {
        if (el instanceof JsonPrimitive prim && prim.isString()) {
            return NamedEntries.BLOCK_SOUND_GROUPS.get(prim.getAsString().toUpperCase(Locale.ROOT));
        } else if (el instanceof JsonObject obj) {
            float volume = JsonHelper.getFloat(obj, "volume");
            float pitch = JsonHelper.getFloat(obj, "pitch");
            SoundEvent breakSound = getSoundEvent(obj, "break_sound");
            SoundEvent stepSound = getSoundEvent(obj, "step_sound");
            SoundEvent placeSound = getSoundEvent(obj, "place_sound");
            SoundEvent hitSound = getSoundEvent(obj, "hit_sound");
            SoundEvent fallSound = getSoundEvent(obj, "fall_sound");

            return new BlockSoundGroup(volume, pitch, breakSound, stepSound, placeSound, hitSound, fallSound);
        } else {
            throw new JsonSyntaxException("Expected sounds to be object or string");
        }
    }

    private static SoundEvent getSoundEvent(JsonObject obj, String key) {
        return Registry.SOUND_EVENT.get(new Identifier(JsonHelper.getString(obj, key)));
    }

    private static ItemGroup readItemGroup(JsonObject obj, String key) {
        String name = JsonHelper.getString(obj, key);
        ItemGroup group = Arrays.stream(ItemGroup.GROUPS).filter(x -> x.getName().equals(name)).findAny().orElse(null);

        if (group == null)
            throw new JsonSyntaxException("Expected " + key + " to be valid item group, got unknown item group name " + name);

        return group;
    }

    private static FoodComponent readFoodComponent(JsonObject obj) {
        FoodComponent.Builder builder = new FoodComponent.Builder();

        builder.hunger(JsonHelper.getInt(obj, "hunger"));
        builder.saturationModifier(JsonHelper.getInt(obj, "saturation_modifier"));

        if (JsonHelper.getBoolean(obj, "meat", false))
            builder.meat();

        if (JsonHelper.getBoolean(obj, "always_edible", false))
            builder.alwaysEdible();

        if (JsonHelper.getBoolean(obj, "snack", false))
            builder.snack();

        if (obj.has("effects")) {
            var effects = JsonHelper.getArray(obj, "effects");

            for (JsonElement el : effects) {
                JsonObject effectObj = JsonHelper.asObject(el, "<effect item>");

                float chance = JsonHelper.getFloat(effectObj, "chance");
                StatusEffectInstance effect = readStatusEffectInstance(effectObj);

                builder.statusEffect(effect, chance);
            }
        }

        return builder.build();
    }

    public static StatusEffectInstance readStatusEffectInstance(JsonObject obj) {
        StatusEffect effect = Registry.STATUS_EFFECT.get(new Identifier(JsonHelper.getString(obj, "effect")));
        byte amplifier = JsonHelper.getByte(obj, "amplifier");
        int duration = JsonHelper.getInt(obj, "duration");
        boolean ambient = JsonHelper.getBoolean(obj, "ambient", false);
        boolean showParticles = JsonHelper.getBoolean(obj, "show_particles", true);
        boolean showIcon = JsonHelper.getBoolean(obj, "show_icon", true);

        return new StatusEffectInstance(effect, duration, amplifier, ambient, showParticles, showIcon);
    }

    public static Map<EntityAttribute, EntityAttributeModifier> readAttributeModifiers(JsonObject obj) {
        Map<EntityAttribute, EntityAttributeModifier> map = new HashMap<>();

        for (var entry : obj.entrySet()) {
            EntityAttribute attribute = Registry.ATTRIBUTE.get(new Identifier(entry.getKey()));

            if (attribute == null) throw new JsonSyntaxException(entry.getKey() + " is an invalid attribute");

            JsonObject modifier = JsonHelper.asObject(entry.getValue(), entry.getKey());

            double value = JsonHelper.getDouble(modifier, "value");
            EntityAttributeModifier.Operation op = switch (JsonHelper.getString(modifier, "operation")) {
                case "addition" -> EntityAttributeModifier.Operation.ADDITION;
                case "multiply_base" -> EntityAttributeModifier.Operation.MULTIPLY_BASE;
                case "multiply_total" -> EntityAttributeModifier.Operation.MULTIPLY_TOTAL;
                default -> throw new IllegalStateException("invalid operation type");
            };
            String name = JsonHelper.getString(modifier, "name");
            UUID uuid = UUID.fromString(JsonHelper.getString(modifier, "uuid"));

            map.put(attribute, new EntityAttributeModifier(uuid, name, value, op));
        }

        return map;
    }
}
