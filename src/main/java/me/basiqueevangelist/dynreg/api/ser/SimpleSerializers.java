package me.basiqueevangelist.dynreg.api.ser;

import me.basiqueevangelist.dynreg.impl.access.ExtendedBlockSettings;
import me.basiqueevangelist.dynreg.impl.util.NamedEntries;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.loot.LootTable;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;

/**
 * Utilities for reading and writing some common types to {@link PacketByteBuf}s.
 */
public class SimpleSerializers {
    public static void writeBlockSettings(PacketByteBuf buf, AbstractBlock.Settings settings) {
        buf.writeVarInt(((ExtendedBlockSettings) settings).dynreg$getMapColor().id);
        buf.writeBoolean(settings.collidable);
        SimpleSerializers.writeBlockSoundGroup(buf, settings.soundGroup);
        buf.writeFloat(settings.resistance);
        buf.writeFloat(settings.hardness);
        buf.writeBoolean(settings.toolRequired);
        buf.writeBoolean(settings.randomTicks);
        buf.writeFloat(settings.slipperiness);
        buf.writeFloat(settings.velocityMultiplier);
        buf.writeFloat(settings.jumpVelocityMultiplier);

        buf.writeBoolean(settings.lootTableKey != null);
        if (settings.lootTableKey != null) {
            buf.writeIdentifier(settings.lootTableKey.getValue());
        }

        buf.writeBoolean(settings.opaque);
        buf.writeBoolean(settings.isAir);
        buf.writeBoolean(settings.dynamicBounds);
        buf.writeBoolean(settings.burnable);
        buf.writeBoolean(settings.forceSolid);
        buf.writeBoolean(settings.forceNotSolid);
        buf.writeBoolean(settings.liquid);
        buf.writeBoolean(settings.blockBreakParticles);
        buf.writeEnumConstant(settings.instrument);
        buf.writeBoolean(settings.replaceable);
    }

    public static AbstractBlock.Settings readBlockSettings(PacketByteBuf buf) {
        MapColor color = MapColor.get(buf.readVarInt());
        boolean collidable = buf.readBoolean();
        BlockSoundGroup soundGroup = SimpleSerializers.readBlockSoundGroup(buf);
        float resistance = buf.readFloat();
        float hardness = buf.readFloat();
        boolean toolRequired = buf.readBoolean();
        boolean randomTicks = buf.readBoolean();
        float slipperiness = buf.readFloat();
        float velocityMultiplier = buf.readFloat();
        float jumpVelocityMultiplier = buf.readFloat();
        RegistryKey<LootTable> lootTableKey;

        if (buf.readBoolean())
            lootTableKey = buf.readRegistryKey(RegistryKeys.LOOT_TABLE);
        else
            lootTableKey = null;

        boolean opaque = buf.readBoolean();
        boolean isAir = buf.readBoolean();
        boolean dynamicBounds = buf.readBoolean();
        boolean burnable = buf.readBoolean();
        boolean forceSolid = buf.readBoolean();
        boolean forceNotSolid = buf.readBoolean();
        boolean liquid = buf.readBoolean();
        boolean blockBreakParticles = buf.readBoolean();
        NoteBlockInstrument instrument = buf.readEnumConstant(NoteBlockInstrument.class);
        boolean replaceable = buf.readBoolean();

        AbstractBlock.Settings settings = AbstractBlock.Settings.create();
        settings.mapColor(color);
        settings.collidable = collidable;
        settings.sounds(soundGroup);
        settings.resistance(resistance);
        settings.hardness(hardness);
        if (toolRequired) settings.requiresTool();
        if (randomTicks) settings.ticksRandomly();
        settings.slipperiness(slipperiness);
        settings.velocityMultiplier(velocityMultiplier);
        settings.jumpVelocityMultiplier(jumpVelocityMultiplier);
        if (lootTableKey != null) settings.lootTableKey = lootTableKey;
        if (!opaque) settings.nonOpaque();
        if (isAir) settings.air();
        if (dynamicBounds) settings.dynamicBounds();
        if (burnable) settings.burnable();
        if (forceSolid) settings.solid();
        if (forceNotSolid) settings.notSolid();
        if (liquid) settings.liquid();
        if (!blockBreakParticles) settings.noBlockBreakParticles();
        settings.instrument(instrument);
        if (replaceable) settings.replaceable();

        return settings;
    }

    public static void writeBlockSoundGroup(PacketByteBuf buf, BlockSoundGroup group) {
        String name = NamedEntries.BLOCK_SOUND_GROUPS.inverse().getOrDefault(group, "");

        buf.writeString(name);

        if (!name.equals("")) return;

        buf.writeFloat(group.volume);
        buf.writeFloat(group.pitch);
        buf.writeVarInt(Registries.SOUND_EVENT.getRawId(group.getBreakSound()));
        buf.writeVarInt(Registries.SOUND_EVENT.getRawId(group.getStepSound()));
        buf.writeVarInt(Registries.SOUND_EVENT.getRawId(group.getPlaceSound()));
        buf.writeVarInt(Registries.SOUND_EVENT.getRawId(group.getHitSound()));
        buf.writeVarInt(Registries.SOUND_EVENT.getRawId(group.getFallSound()));
    }

    public static BlockSoundGroup readBlockSoundGroup(PacketByteBuf buf) {
        String name = buf.readString();

        if (!name.equals("")) return NamedEntries.BLOCK_SOUND_GROUPS.get(name);

        float volume = buf.readFloat();
        float pitch = buf.readFloat();
        SoundEvent breakSound = Registries.SOUND_EVENT.get(buf.readVarInt());
        SoundEvent stepSound = Registries.SOUND_EVENT.get(buf.readVarInt());
        SoundEvent placeSound = Registries.SOUND_EVENT.get(buf.readVarInt());
        SoundEvent hitSound = Registries.SOUND_EVENT.get(buf.readVarInt());
        SoundEvent fallSound = Registries.SOUND_EVENT.get(buf.readVarInt());

        return new BlockSoundGroup(volume, pitch, breakSound, stepSound, placeSound, hitSound, fallSound);
    }

    public static void writeAttributeModifiers(PacketByteBuf buf, Map<RegistryEntry<EntityAttribute>, EntityAttributeModifier> map) {
        buf.writeMap(map,
            (buf2, key) -> buf2.writeIdentifier(key.getKey().orElseThrow().getValue()),
            (buf2, modifier) -> {
                buf2.writeDouble(modifier.value());
                buf2.writeEnumConstant(modifier.operation());
                buf2.writeIdentifier(modifier.id());
            });
    }

    public static Map<RegistryEntry<EntityAttribute>, EntityAttributeModifier> readAttributeModifiers(PacketByteBuf buf) {
        return buf.readMap(
            (buf2) -> Registries.ATTRIBUTE.getEntry(buf2.readIdentifier()).orElseThrow(),
            (buf2) -> {
                double value = buf2.readDouble();
                EntityAttributeModifier.Operation op = buf2.readEnumConstant(EntityAttributeModifier.Operation.class);
                Identifier id = buf2.readIdentifier();
                return new EntityAttributeModifier(id, value, op);
            });
    }

    public static void writeEntityDimensions(PacketByteBuf buf, EntityDimensions dimensions) {
        buf.writeFloat(dimensions.width());
        buf.writeFloat(dimensions.height());
        buf.writeFloat(dimensions.eyeHeight());
        buf.writeBoolean(dimensions.fixed());
    }

    public static EntityDimensions readEntityDimensions(PacketByteBuf buf) {
        float width = buf.readFloat();
        float height = buf.readFloat();
        float eyeHight = buf.readFloat();
        boolean fixed = buf.readBoolean();

        EntityDimensions dims = fixed ? EntityDimensions.fixed(width, height) : EntityDimensions.changing(width, height);

        return dims.withEyeHeight(eyeHight);
    }
}
