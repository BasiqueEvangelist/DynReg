package me.basiqueevangelist.dynreg.mixin.palette;

import me.basiqueevangelist.dynreg.access.CleanablePalette;
import me.basiqueevangelist.dynreg.access.ExtendedIdListPalette;
import me.basiqueevangelist.dynreg.fixer.BlockFixer;
import me.basiqueevangelist.dynreg.fixer.RemovedStateIdList;
import net.minecraft.world.chunk.IdListPalette;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(IdListPalette.class)
public class IdListPaletteMixin<T> implements CleanablePalette<T>, ExtendedIdListPalette {
    private RemovedStateIdList dynreg$removedBlockList = new RemovedStateIdList();

    @Override
    public void dynreg$cleanDeletedElements(Function<T, T> defaultElement) {
        // Do nothing.
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void useOldState(int id, CallbackInfoReturnable<T> cir) {
        if (dynreg$removedBlockList != null) {
            var removedStates = dynreg$removedBlockList.getRemovedStateIds();

            var removedState = removedStates.get(id);

            if (removedState != null)
                cir.setReturnValue((T) removedState);
        }
    }

    @Override
    public void dynreg$updateVersion() {
        dynreg$removedBlockList.getRemovedStateIds().clear();
    }

    @Override
    public void dynreg$markAsBiome() {
        dynreg$removedBlockList = null;

        // TODO: biome support.
    }
}
