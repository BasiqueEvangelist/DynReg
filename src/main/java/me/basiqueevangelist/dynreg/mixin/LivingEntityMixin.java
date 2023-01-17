package me.basiqueevangelist.dynreg.mixin;

import me.basiqueevangelist.dynreg.fixer.StatusEffectFixer;
import me.basiqueevangelist.dynreg.util.AdaptUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Shadow @Final private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;
    private int dynreg$effectsVersion = StatusEffectFixer.EFFECTS_VERSION.getVersion();

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = {"tickStatusEffects", "updatePotionVisibility", "clearStatusEffects", "getStatusEffects",
        "getActiveStatusEffects", "hasStatusEffect", "getStatusEffect", "addStatusEffect*", "setStatusEffect"}, at = @At("HEAD"))
    private void effectHook(CallbackInfoReturnable<?> cir) {
        checkEffects();
    }

    @Unique
    private void checkEffects() {
        int currentVersion = StatusEffectFixer.EFFECTS_VERSION.getVersion();

        if (dynreg$effectsVersion != currentVersion) {
            dynreg$effectsVersion = currentVersion;

            Map<StatusEffect, StatusEffectInstance> newEffects = new HashMap<>();

            activeStatusEffects.entrySet().removeIf(x -> {
                if (x.getKey().wasDeleted()) {
                    StatusEffect adapted = AdaptUtil.adaptEffect(x.getKey());

                    if (adapted != null)
                        newEffects.put(adapted, x.getValue());

                    return true;
                }

                return false;
            });

            activeStatusEffects.putAll(newEffects);
        }
    }
}
