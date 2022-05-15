package me.basiqueevangelist.dynreg.network;

import me.basiqueevangelist.dynreg.access.ExtendedBlockSettings;
import me.basiqueevangelist.dynreg.util.NamedEntries;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;

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

    public static void writeFoodComponent(PacketByteBuf buf, FoodComponent component) {
        byte flags = 0;

        if (component.isMeat()) flags |= 1;
        if (component.isAlwaysEdible()) flags |= 2;
        if (component.isSnack()) flags |= 4;

        buf.writeVarInt(component.getHunger());
        buf.writeFloat(component.getSaturationModifier());
        buf.writeByte(flags);
        buf.writeVarInt(component.getStatusEffects().size());

        for (var pair : component.getStatusEffects()) {
            writeStatusEffectInstance(buf, pair.getFirst());
            buf.writeFloat(pair.getSecond());
        }
    }

    public static FoodComponent readFoodComponent(PacketByteBuf buf) {
        int hunger = buf.readVarInt();
        float saturationModifier = buf.readFloat();
        byte flags = buf.readByte();
        boolean isMeat = (flags & 1) != 0;
        boolean isAlwaysEdible = (flags & 1) != 0;
        boolean isSnack = (flags & 1) != 0;
        int statusEffectsCount = buf.readVarInt();

        FoodComponent.Builder builder = new FoodComponent.Builder();
        builder.hunger(hunger);
        builder.saturationModifier(saturationModifier);
        if (isMeat) builder.meat();
        if (isAlwaysEdible) builder.alwaysEdible();
        if (isSnack) builder.snack();

        for (int i = 0; i < statusEffectsCount; i++) {
            StatusEffectInstance effect = readStatusEffectInstance(buf);
            float chance = buf.readFloat();

            builder.statusEffect(effect, chance);
        }

        return builder.build();
    }

    public static void writeStatusEffectInstance(PacketByteBuf buf, StatusEffectInstance effect) {
        buf.writeVarInt(StatusEffect.getRawId(effect.getEffectType()));
        buf.writeByte((byte)(effect.getAmplifier() & 0xff));
        buf.writeVarInt(Math.min(effect.getDuration(), 32767));

        byte flags = 0;

        if (effect.isAmbient()) flags |= 1;
        if (effect.shouldShowParticles()) flags |= 2;
        if (effect.shouldShowIcon()) flags |= 4;

        buf.writeByte(flags);
    }

    public static StatusEffectInstance readStatusEffectInstance(PacketByteBuf buf) {
        StatusEffect effect = StatusEffect.byRawId(buf.readVarInt());
        byte amplifier = buf.readByte();
        int duration = buf.readVarInt();
        byte flags = buf.readByte();
        boolean ambient = (flags & 1) != 0;
        boolean showParticles = (flags & 2) != 0;
        boolean showIcon = (flags & 4) != 0;

        return new StatusEffectInstance(effect, duration, amplifier, ambient, showParticles, showIcon);
    }

    @SuppressWarnings("deprecation")
    public static void writeItemSettings(PacketByteBuf buf, Item.Settings settings) {
        buf.writeVarInt(settings.maxCount);
        buf.writeVarInt(settings.maxDamage);

        buf.writeBoolean(settings.recipeRemainder != null);
        if (settings.recipeRemainder != null) {
            buf.writeIdentifier(settings.recipeRemainder.getRegistryEntry().registryKey().getValue());
        }

        buf.writeString(settings.group == null ? "" : settings.group.getName());
        buf.writeString(NamedEntries.RARITIES.inverse().get(settings.rarity));

        buf.writeBoolean(settings.foodComponent != null);
        if (settings.foodComponent != null) {
            SimpleSerializers.writeFoodComponent(buf, settings.foodComponent);
        }

        buf.writeBoolean(settings.fireproof);
    }

    public static Item.Settings readItemSettings(PacketByteBuf buf) {
        int maxCount = buf.readVarInt();
        int maxDamage = buf.readVarInt();

        Item recipeRemainder = null;
        if (buf.readBoolean())
            recipeRemainder = Registry.ITEM.get(buf.readIdentifier());

        ItemGroup group = null;
        String groupName = buf.readString();
        if (!groupName.equals(""))
            group = Arrays.stream(ItemGroup.GROUPS).filter(x -> x.getName().equals(groupName)).findAny().orElseThrow();

        Rarity rarity = NamedEntries.RARITIES.get(buf.readString());

        FoodComponent component = null;
        if (buf.readBoolean())
            component = readFoodComponent(buf);

        boolean fireproof = buf.readBoolean();

        Item.Settings settings = new Item.Settings();

        settings.maxCount(maxCount);
        if (maxDamage > 0) settings.maxDamage(maxDamage);
        if (recipeRemainder != null) settings.recipeRemainder(recipeRemainder);
        if (group != null) settings.group(group);
        settings.rarity(rarity);
        if (component != null) settings.food(component);
        if (fireproof) settings.fireproof();

        return settings;
    }
}
