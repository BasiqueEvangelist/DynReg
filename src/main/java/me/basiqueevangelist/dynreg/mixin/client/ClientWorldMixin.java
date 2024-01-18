package me.basiqueevangelist.dynreg.mixin.client;

import me.basiqueevangelist.dynreg.impl.access.ExtendedEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Inject(method = "tickEntity", at = @At("RETURN"))
    private void tryReplace(Entity entity, CallbackInfo ci) {
        ((ExtendedEntity) entity).dynreg$tryReplace();
    }

    @Inject(method = "tickPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tickRiding()V", shift = At.Shift.AFTER))
    private void tryReplace(Entity vehicle, Entity passenger, CallbackInfo ci) {
        ((ExtendedEntity) passenger).dynreg$tryReplace();
    }
}
