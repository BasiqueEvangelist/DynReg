package me.basiqueevangelist.dynreg.api.ser;

import com.google.gson.*;
import me.basiqueevangelist.dynreg.util.NamedEntries;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
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
        FabricBlockSettings settings = FabricBlockSettings.create();

        if (obj.has("color"))
            settings.mapColor(NamedEntries.MAP_COLORS.get(JsonHelper.getString(obj, "color").toUpperCase(Locale.ROOT)));

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
            settings.instrument(NamedEntries.INSTRUMENTS.get(JsonHelper.getString(obj, "instrument")));

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
        return Registries.SOUND_EVENT.get(new Identifier(JsonHelper.getString(obj, key)));
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

    public static Map<EntityAttribute, EntityAttributeModifier> readAttributeModifiers(JsonObject obj) {
        Map<EntityAttribute, EntityAttributeModifier> map = new HashMap<>();

        for (var entry : obj.entrySet()) {
            EntityAttribute attribute = Registries.ATTRIBUTE.get(new Identifier(entry.getKey()));

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

    public static EntityDimensions readEntityDimensions(JsonElement value) {
        if (value instanceof JsonArray array) {
            return EntityDimensions.fixed(array.get(0).getAsFloat(), array.get(1).getAsFloat());
        } else {
            var obj = JsonHelper.asObject(value, "dimensions");

            return new EntityDimensions(
                JsonHelper.getFloat(obj, "width"),
                JsonHelper.getFloat(obj, "height"),
                JsonHelper.getBoolean(obj, "fixed", true)
            );
        }
    }
}
