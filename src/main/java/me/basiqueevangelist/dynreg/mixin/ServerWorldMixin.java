package me.basiqueevangelist.dynreg.mixin;

import me.basiqueevangelist.dynreg.impl.access.ExtendedEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Inject(method = "tickEntity", at = @At("RETURN"))
    private void tryReplace(Entity entity, CallbackInfo ci) {
        ((ExtendedEntity) entity).dynreg$tryReplace();
    }

    @Inject(method = "tickPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tickRiding()V", shift = At.Shift.AFTER))
    private void tryReplace(Entity vehicle, Entity passenger, CallbackInfo ci) {
        ((ExtendedEntity) passenger).dynreg$tryReplace();
    }
}
