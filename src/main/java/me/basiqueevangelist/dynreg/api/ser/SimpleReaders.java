package me.basiqueevangelist.dynreg.api.ser;

import com.google.gson.*;
import me.basiqueevangelist.dynreg.impl.util.NamedEntries;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Utilities for reading common types from JSON.
 */
public final class SimpleReaders {
    private SimpleReaders() {

    }

    public static AbstractBlock.Settings readBlockSettings(JsonObject obj) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create();

        if (obj.has("color"))
            settings.mapColor(NamedEntries.MAP_COLORS.get(JsonHelper.getString(obj, "color").toUpperCase(Locale.ROOT)));

        if (obj.has("collidable"))
            settings.collidable = JsonHelper.getBoolean(obj, "collidable");

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
            settings.lootTableKey = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(JsonHelper.getString(obj, "drops_like")));

        if (JsonHelper.getBoolean(obj, "non_opaque", false))
            settings.nonOpaque();

        if (JsonHelper.getBoolean(obj, "air", false))
            settings.air();

        if (JsonHelper.getBoolean(obj, "dynamic_bounds", false))
            settings.dynamicBounds();

        if (JsonHelper.getBoolean(obj, "burnable", false))
            settings.burnable();

        if (obj.has("solid")) {
            if (JsonHelper.getBoolean(obj, "solid"))
                settings.solid();
            else
                settings.notSolid();
        }

        if (JsonHelper.getBoolean(obj, "liquid", false))
            settings.liquid();

        // OFFSETTER

        if (JsonHelper.getBoolean(obj, "no_break_particles", false))
            settings.noBlockBreakParticles();

        if (obj.has("instrument"))
            settings.instrument(NamedEntries.NOTE_BLOCK_INSTRUMENTS.get(JsonHelper.getString(obj, "instrument")));

        if (JsonHelper.getBoolean(obj, "replaceable", false))
            settings.replaceable();

        return settings;
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
        return Registries.SOUND_EVENT.get(Identifier.of(JsonHelper.getString(obj, key)));
    }

//    private static ItemGroup readItemGroup(JsonObject obj, String key) {
//        String name = JsonHelper.getString(obj, key);
//        ItemGroup group = Arrays.stream(ItemGroup.GROUPS).filter(x -> x.getName().equals(name)).findAny().orElse(null);
//
//        if (group == null)
//            throw new JsonSyntaxException("Expected " + key + " to be valid item group, got unknown item group name " + name);
//
//        return group;
//    }

    public static Map<RegistryEntry<EntityAttribute>, EntityAttributeModifier> readAttributeModifiers(JsonObject obj) {
        Map<RegistryEntry<EntityAttribute>, EntityAttributeModifier> map = new HashMap<>();

        for (var entry : obj.entrySet()) {
            RegistryEntry<EntityAttribute> attribute = Registries.ATTRIBUTE.getEntry(Identifier.of(entry.getKey())).orElse(null);

            if (attribute == null) throw new JsonSyntaxException(entry.getKey() + " is an invalid attribute");

            JsonObject modifier = JsonHelper.asObject(entry.getValue(), entry.getKey());

            double value = JsonHelper.getDouble(modifier, "value");
            EntityAttributeModifier.Operation op = switch (JsonHelper.getString(modifier, "operation")) {
                case "addition" -> EntityAttributeModifier.Operation.ADD_VALUE;
                case "multiply_base" -> EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                case "multiply_total" -> EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
                default -> throw new IllegalStateException("invalid operation type");
            };
            Identifier id = Identifier.of(JsonHelper.getString(modifier, "id"));

            map.put(attribute, new EntityAttributeModifier(id, value, op));
        }

        return map;
    }

    public static EntityDimensions readEntityDimensions(JsonElement value) {
        if (value instanceof JsonArray array) {
            return EntityDimensions.fixed(array.get(0).getAsFloat(), array.get(1).getAsFloat());
        } else {
            var obj = JsonHelper.asObject(value, "dimensions");

            float width = JsonHelper.getFloat(obj, "width");
            float height = JsonHelper.getFloat(obj, "height");

            return
                JsonHelper.getBoolean(obj, "fixed", true)
                    ? EntityDimensions.fixed(width, height)
                    : EntityDimensions.changing(width, height);
        }
    }
}
