package me.basiqueevangelist.dynreg.wrapped;

import me.basiqueevangelist.dynreg.access.ExtendedBlockSettings;
import me.basiqueevangelist.dynreg.util.NamedEntries;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;

public class SimpleSerializers {
    public static void writeBlockSettings(PacketByteBuf buf, AbstractBlock.Settings settings) {
        SimpleSerializers.writeMaterial(buf, settings.material);
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

        buf.writeBoolean(settings.lootTableId != null);
        if (settings.lootTableId != null) {
            buf.writeIdentifier(settings.lootTableId);
        }

        buf.writeBoolean(settings.opaque);
        buf.writeBoolean(settings.isAir);
        buf.writeBoolean(settings.dynamicBounds);
    }

    public static AbstractBlock.Settings readBlockSettings(PacketByteBuf buf) {
        Material material = SimpleSerializers.readMaterial(buf);
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
        Identifier lootTableId;

        if (buf.readBoolean())
            lootTableId = buf.readIdentifier();
        else
            lootTableId = null;

        boolean opaque = buf.readBoolean();
        boolean isAir = buf.readBoolean();
        boolean dynamicBounds = buf.readBoolean();

        FabricBlockSettings settings = FabricBlockSettings.of(material, color);
        settings.collidable(collidable);
        settings.sounds(soundGroup);
        settings.resistance(resistance);
        settings.hardness(hardness);
        if (toolRequired) settings.requiresTool();
        if (randomTicks) settings.ticksRandomly();
        settings.slipperiness(slipperiness);
        settings.velocityMultiplier(velocityMultiplier);
        settings.jumpVelocityMultiplier(jumpVelocityMultiplier);
        if (lootTableId != null) settings.drops(lootTableId);
        if (!opaque) settings.nonOpaque();
        if (isAir) settings.air();
        if (dynamicBounds) settings.dynamicBounds();

        return settings;
    }

    public static void writeMaterial(PacketByteBuf buf, Material material) {
        String name = NamedEntries.MATERIALS.inverse().getOrDefault(material, "");

        buf.writeString(name);

        if (!name.equals("")) return;

        buf.writeBoolean(material.isLiquid());
        buf.writeBoolean(material.isSolid());
        buf.writeBoolean(material.blocksMovement());
        buf.writeBoolean(material.isBurnable());
        buf.writeBoolean(material.isReplaceable());
        buf.writeBoolean(material.blocksLight());
        buf.writeEnumConstant(material.getPistonBehavior());
        buf.writeVarInt(material.getColor().id);
    }

    public static Material readMaterial(PacketByteBuf buf) {
        String name = buf.readString();

        if (!name.equals("")) return NamedEntries.MATERIALS.get(name);

        boolean isLiquid = buf.readBoolean();
        boolean isSolid = buf.readBoolean();
        boolean blocksMovement = buf.readBoolean();
        boolean isBurnable = buf.readBoolean();
        boolean isReplacable = buf.readBoolean();
        boolean blocksLight = buf.readBoolean();
        PistonBehavior pistonBehaviour = buf.readEnumConstant(PistonBehavior.class);
        MapColor color = MapColor.get(buf.readVarInt());

        var builder = new Material.Builder(color);

        if (isLiquid) builder.liquid();
        if (!isSolid) builder.notSolid();
        if (!blocksMovement) builder.allowsMovement();
        if (isBurnable) builder.burnable();
        if (isReplacable) builder.replaceable();
        if (blocksLight) builder.lightPassesThrough();
        builder.pistonBehavior = pistonBehaviour;

        return builder.build();
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

    public static void writeAttributeModifiers(PacketByteBuf buf, Map<EntityAttribute, EntityAttributeModifier> map) {
        buf.writeMap(map,
            (buf2, key) -> buf2.writeIdentifier(Registries.ATTRIBUTE.getId(key)),
            (buf2, modifier) -> {
                buf2.writeDouble(modifier.getValue());
                buf2.writeEnumConstant(modifier.getOperation());
                buf2.writeString(modifier.getName());
                buf2.writeUuid(modifier.getId());
            });
    }

    public static Map<EntityAttribute, EntityAttributeModifier> readAttributeModifiers(PacketByteBuf buf) {
        return buf.readMap(
            (buf2) -> Registries.ATTRIBUTE.get(buf2.readIdentifier()),
            (buf2) -> {
                double value = buf2.readDouble();
                EntityAttributeModifier.Operation op = buf2.readEnumConstant(EntityAttributeModifier.Operation.class);
                String name = buf2.readString();
                UUID uuid = buf2.readUuid();
                return new EntityAttributeModifier(uuid, name, value, op);
            });
    }

    public static void writeEntityDimensions(PacketByteBuf buf, EntityDimensions dimensions) {
        buf.writeFloat(dimensions.width);
        buf.writeFloat(dimensions.height);
        buf.writeBoolean(dimensions.fixed);
    }

    public static EntityDimensions readEntityDimensions(PacketByteBuf buf) {
        return new EntityDimensions(buf.readFloat(), buf.readFloat(), buf.readBoolean());
    }
}
