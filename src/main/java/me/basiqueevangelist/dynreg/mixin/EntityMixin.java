package me.basiqueevangelist.dynreg.mixin;

import me.basiqueevangelist.dynreg.impl.access.DeletableObjectInternal;
import me.basiqueevangelist.dynreg.impl.access.ExtendedEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements ExtendedEntity {
    @Shadow @Final private EntityType<?> type;

    @Shadow public abstract void discard();

    @Shadow public World world;

    @Inject(method = "getSavedEntityId", at = @At("HEAD"), cancellable = true)
    private void useDeletedId(CallbackInfoReturnable<String> cir) {
        if (type.isSaveable() && type.wasDeleted()) {
            cir.setReturnValue(((DeletableObjectInternal) type).dynreg$getId().toString());
        }
    }

    @Override
    public void dynreg$tryReplace() {
        if (type.wasDeleted()) {
            var newType = Registries.ENTITY_TYPE
                .getOrEmpty(((DeletableObjectInternal) type).dynreg$getId())
                .orElse(null);

            discard();

            if (newType != null && !world.isClient) {
                var newEntity = newType.create(world);
                newEntity.copyFrom((Entity) (Object) this);
                world.spawnEntity(newEntity);
            }
        }
    }
}
