package me.basiqueevangelist.dynreg.mixin.palette;

import me.basiqueevangelist.dynreg.access.CleanablePalette;
import me.basiqueevangelist.dynreg.access.ExtendedIdListPalette;
import me.basiqueevangelist.dynreg.fixer.BlockFixer;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.IdList;
import net.minecraft.world.chunk.IdListPalette;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(IdListPalette.class)
public class IdListPaletteMixin<T> implements CleanablePalette<T>, ExtendedIdListPalette {
    private IdList<BlockState> dynreg$blockIdList = BlockFixer.CURRENT_STATES_LIST;

    @Override
    public void dynreg$cleanDeletedElements(Function<T, T> defaultElement) {
        // Do nothing.
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void useOldState(int id, CallbackInfoReturnable<T> cir) {
        if (dynreg$blockIdList != null) {
            BlockState state = dynreg$blockIdList.get(id);

            if (state != null)
                cir.setReturnValue((T) state);
        }
    }

    @Override
    public void dynreg$updateVersion() {
        dynreg$blockIdList = BlockFixer.CURRENT_STATES_LIST;
    }

    @Override
    public void dynreg$markAsBiome() {
        dynreg$blockIdList = null;

        // TODO: biome support.
    }
}
