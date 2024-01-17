package me.basiqueevangelist.dynreg.testmod.desc;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.api.entry.EntryRegisterContext;
import me.basiqueevangelist.dynreg.api.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.api.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.api.ser.SimpleHashers;
import me.basiqueevangelist.dynreg.api.ser.SimpleReaders;
import me.basiqueevangelist.dynreg.testmod.DynRegTest;
import me.basiqueevangelist.dynreg.api.ser.SimpleSerializers;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.Map;

public class StatusEffectEntry implements RegistrationEntry {
    public static final Identifier ID = DynRegTest.id("status_effect");

    private final Identifier id;
    private final StatusEffectCategory category;
    private final int color;
    private final Map<EntityAttribute, EntityAttributeModifier> modifiers;

    public StatusEffectEntry(Identifier id, JsonObject obj) {
        this.id = id;
        this.category = switch (JsonHelper.getString(obj, "category")) {
            case "beneficial" -> StatusEffectCategory.BENEFICIAL;
            case "harmful" -> StatusEffectCategory.HARMFUL;
            case "neutral" -> StatusEffectCategory.NEUTRAL;
            default -> throw new IllegalStateException("invalid category value");
        };
        this.color = JsonHelper.getInt(obj, "color");

        this.modifiers
            = SimpleReaders.readAttributeModifiers(JsonHelper.getObject(obj, "modifiers", new JsonObject()));
    }

    public StatusEffectEntry(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.category = buf.readEnumConstant(StatusEffectCategory.class);
        this.color = buf.readInt();
        this.modifiers = SimpleSerializers.readAttributeModifiers(buf);
    }

    @Override
    public void scan(EntryScanContext ctx) {
        ctx.announce(Registries.STATUS_EFFECT, id);
    }

    @Override
    public void register(EntryRegisterContext ctx) {
        StatusEffect effect = new StatusEffect(category, color) {};

        effect.getAttributeModifiers().putAll(modifiers);

        ctx.register(Registries.STATUS_EFFECT, id, effect);
    }

    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(category);
        buf.writeInt(color);
        SimpleSerializers.writeAttributeModifiers(buf, modifiers);
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public long hash() {
        int hash = id.hashCode();
        hash = 31 * hash + category.hashCode();
        hash = 31 * hash + color;
        hash = 31 * hash + SimpleHashers.hash(modifiers);
        return hash;
    }
}
