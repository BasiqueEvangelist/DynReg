package me.basiqueevangelist.dynreg.testmod.desc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.entry.*;
import me.basiqueevangelist.dynreg.wrapped.LazyStatusEffectInstance;
import me.basiqueevangelist.dynreg.testmod.DynRegTest;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PotionEntry implements RegistrationEntry {
    public static final Identifier ID = DynRegTest.id("potion");

    private final Identifier id;
    private final @Nullable String baseName;
    private final List<LazyStatusEffectInstance> effects;

    public PotionEntry(Identifier id, JsonObject object) {
        this.id = id;
        this.baseName = JsonHelper.getString(object, "base_name", null);
        this.effects = new ArrayList<>();

        var arr = JsonHelper.getArray(object, "effects");

        for (JsonElement el : arr) {
            JsonObject effectObj = JsonHelper.asObject(el, "<effect item>");

            effects.add(new LazyStatusEffectInstance(effectObj));
        }
    }

    public PotionEntry(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.baseName = buf.readNullable(PacketByteBuf::readString);
        this.effects = buf.readList(LazyStatusEffectInstance::new);
    }

    @Override
    public void scan(EntryScanContext ctx) {
        ctx.announce(Registries.POTION, id);

        effects.forEach(x -> x.scan(ctx));
    }

    @Override
    public void register(EntryRegisterContext ctx) {
        Potion potion = new Potion(baseName, effects
            .stream()
            .map(LazyStatusEffectInstance::build)
            .toArray(StatusEffectInstance[]::new));

        ctx.register(Registries.POTION, id, potion);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeNullable(baseName, PacketByteBuf::writeString);
        buf.writeCollection(effects, (buf1, inst) -> inst.write(buf1));
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public int hash() {
        int hash = id.hashCode();
        hash = 31 * hash + Objects.hashCode(baseName);
        hash = 31 * hash + effects.hashCode();

        return hash;
    }
}
