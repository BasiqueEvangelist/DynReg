package me.basiqueevangelist.dynreg.wrapped;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.util.LazyEntryRef;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class LazyStatusEffectInstance {
    private final LazyEntryRef<StatusEffect> type;
    private final int duration;
    private int amplifier;
    private boolean ambient;
    private boolean showParticles;
    private boolean showIcon;

    public LazyStatusEffectInstance(LazyEntryRef<StatusEffect> type, int duration) {
        this.type = type;
        this.duration = duration;
    }

    public LazyStatusEffectInstance(PacketByteBuf buf) {
        this.type = LazyEntryRef.read(buf, Registries.STATUS_EFFECT);
        this.amplifier = buf.readByte();
        this.duration = buf.readVarInt();
        byte flags = buf.readByte();
        this.ambient = (flags & 1) != 0;
        this.showParticles = (flags & 2) != 0;
        this.showIcon = (flags & 4) != 0;
    }

    public LazyStatusEffectInstance(JsonObject obj) {
        this.type = new LazyEntryRef<>(Registries.STATUS_EFFECT, new Identifier(JsonHelper.getString(obj, "effect")));
        this.amplifier = JsonHelper.getByte(obj, "amplifier");
        this.duration = JsonHelper.getInt(obj, "duration");
        this.ambient = JsonHelper.getBoolean(obj, "ambient", false);
        this.showParticles = JsonHelper.getBoolean(obj, "show_particles", true);
        this.showIcon = JsonHelper.getBoolean(obj, "show_icon", true);
    }

    public LazyEntryRef<StatusEffect> type() {
        return type;
    }

    public int duration() {
        return duration;
    }

    public int amplifier() {
        return amplifier;
    }

    public LazyStatusEffectInstance amplifier(int amplifier) {
        this.amplifier = amplifier;
        return this;
    }

    public boolean ambient() {
        return ambient;
    }

    public LazyStatusEffectInstance ambient(boolean ambient) {
        this.ambient = ambient;
        return this;
    }

    public boolean showParticles() {
        return showParticles;
    }

    public LazyStatusEffectInstance showParticles(boolean showParticles) {
        this.showParticles = showParticles;
        return this;
    }

    public boolean showIcon() {
        return showIcon;
    }

    public LazyStatusEffectInstance showIcon(boolean showIcon) {
        this.showIcon = showIcon;
        return this;
    }

    public void write(PacketByteBuf buf) {
        type.write(buf);
        buf.writeByte((byte) (amplifier & 0xff));
        buf.writeVarInt(Math.min(duration, 32767));

        byte flags = 0;

        if (ambient) flags |= 1;
        if (showParticles) flags |= 2;
        if (showIcon) flags |= 4;

        buf.writeByte(flags);
    }

    public void scan(EntryScanContext ctx) {
        ctx.dependency(type);
    }

    public StatusEffectInstance build() {
        return new StatusEffectInstance(type.get(), duration, amplifier, ambient, showParticles, showIcon);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LazyStatusEffectInstance that = (LazyStatusEffectInstance) o;

        if (duration != that.duration) return false;
        if (amplifier != that.amplifier) return false;
        if (ambient != that.ambient) return false;
        if (showParticles != that.showParticles) return false;
        if (showIcon != that.showIcon) return false;
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + duration;
        result = 31 * result + amplifier;
        result = 31 * result + (ambient ? 1 : 0);
        result = 31 * result + (showParticles ? 1 : 0);
        result = 31 * result + (showIcon ? 1 : 0);
        return result;
    }
}
