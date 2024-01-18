package me.basiqueevangelist.dynreg.mixin;

import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import me.basiqueevangelist.dynreg.impl.fixer.BlockFixer;
import me.basiqueevangelist.dynreg.impl.util.VersionTracker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowableFluid.class)
public class FlowableFluidMixin {
    @Shadow
    @Final
    private static ThreadLocal<Object2ByteLinkedOpenHashMap<Block.NeighborGroup>> field_15901;
    private static final VersionTracker.ThreadLocalChecker dynreg$checker = BlockFixer.BLOCKS_VERSION.threadLocalChecker();

    @Inject(method = "receivesFlow", at = @At(value = "FIELD", target = "Lnet/minecraft/fluid/FlowableFluid;field_15901:Ljava/lang/ThreadLocal;"))
    private void checkCullMapVersion(Direction face, BlockView world, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState, CallbackInfoReturnable<Boolean> cir) {
        if (dynreg$checker.isUpdateNeeded()) {
            field_15901.remove();
        }
    }
}
