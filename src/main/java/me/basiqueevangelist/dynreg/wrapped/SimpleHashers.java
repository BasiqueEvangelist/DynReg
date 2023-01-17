package me.basiqueevangelist.dynreg.wrapped;

import io.netty.buffer.ByteBufUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;

import java.util.Map;

// TODO: make this not use PacketByteBufs.
public class SimpleHashers {
    public static int hash(AbstractBlock.Settings settings) {
        PacketByteBuf buf = PacketByteBufs.create();
        SimpleSerializers.writeBlockSettings(buf, settings);
        return ByteBufUtil.hashCode(buf);
    }

    public static int hash(Item.Settings settings) {
        PacketByteBuf buf = PacketByteBufs.create();
        SimpleSerializers.writeItemSettings(buf, settings);
        return ByteBufUtil.hashCode(buf);
    }

    public static int hash(Map<EntityAttribute, EntityAttributeModifier> modifiers) {
        PacketByteBuf buf = PacketByteBufs.create();
        SimpleSerializers.writeAttributeModifiers(buf, modifiers);
        return ByteBufUtil.hashCode(buf);
    }

    public static int hash(StatusEffectInstance modifiers) {
        PacketByteBuf buf = PacketByteBufs.create();
        SimpleSerializers.writeStatusEffectInstance(buf, modifiers);
        return ByteBufUtil.hashCode(buf);
    }
}
