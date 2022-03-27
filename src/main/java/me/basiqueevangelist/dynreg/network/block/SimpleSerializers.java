package me.basiqueevangelist.dynreg.network.block;

import me.basiqueevangelist.dynreg.access.ExtendedBlockSettings;
import me.basiqueevangelist.dynreg.util.NamedEntries;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SimpleSerializers {
    public static void writeSettings(PacketByteBuf buf, AbstractBlock.Settings settings) {
        ((ExtendedBlockSettings) settings).dynreg$write(buf);
    }

    public static AbstractBlock.Settings readSettings(PacketByteBuf buf) {
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
        buf.writeVarInt(Registry.SOUND_EVENT.getRawId(group.getBreakSound()));
        buf.writeVarInt(Registry.SOUND_EVENT.getRawId(group.getStepSound()));
        buf.writeVarInt(Registry.SOUND_EVENT.getRawId(group.getPlaceSound()));
        buf.writeVarInt(Registry.SOUND_EVENT.getRawId(group.getHitSound()));
        buf.writeVarInt(Registry.SOUND_EVENT.getRawId(group.getFallSound()));
    }

    public static BlockSoundGroup readBlockSoundGroup(PacketByteBuf buf) {
        String name = buf.readString();

        if (!name.equals("")) return NamedEntries.BLOCK_SOUND_GROUPS.get(name);

        float volume = buf.readFloat();
        float pitch = buf.readFloat();
        SoundEvent breakSound = Registry.SOUND_EVENT.get(buf.readVarInt());
        SoundEvent stepSound = Registry.SOUND_EVENT.get(buf.readVarInt());
        SoundEvent placeSound = Registry.SOUND_EVENT.get(buf.readVarInt());
        SoundEvent hitSound = Registry.SOUND_EVENT.get(buf.readVarInt());
        SoundEvent fallSound = Registry.SOUND_EVENT.get(buf.readVarInt());

        return new BlockSoundGroup(volume, pitch, breakSound, stepSound, placeSound, hitSound, fallSound);
    }
}
