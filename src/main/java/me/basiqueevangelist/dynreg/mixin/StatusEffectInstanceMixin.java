package me.basiqueevangelist.dynreg.mixin;

import me.basiqueevangelist.dynreg.fixer.StatusEffectFixer;
import me.basiqueevangelist.dynreg.util.AdaptUtil;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectInstance.class)
public class StatusEffectInstanceMixin {
    @Mutable
    @Shadow @Final private StatusEffect type;
    @Shadow int duration;

    private int dynreg$effectsVersion = StatusEffectFixer.EFFECTS_VERSION.getVersion();

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = {"getEffectType", "upgrade", "copyFrom", "update"}, at = @At("HEAD"))
    private void effectHook(CallbackInfoReturnable<?> cir) {
        checkEffect();
    }

    @Unique
    private void checkEffect() {
        int currentVersion = StatusEffectFixer.EFFECTS_VERSION.getVersion();

        if (dynreg$effectsVersion != currentVersion) {
            dynreg$effectsVersion = currentVersion;

            if (type != null && type.wasDeleted()) {
                type = AdaptUtil.adaptEffect(type);

                if (type == null) {
                    duration = 0;
                    type = StatusEffects.SPEED;
                }
            }
        }
    }
}
