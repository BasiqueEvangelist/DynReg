package me.basiqueevangelist.dynreg.mixin;

import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import me.basiqueevangelist.dynreg.fixer.BlockFixer;
import me.basiqueevangelist.dynreg.util.VersionTracker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {
    @Shadow @Final private static ThreadLocal<Object2ByteLinkedOpenHashMap<Block.NeighborGroup>> FACE_CULL_MAP;
    private static final VersionTracker.ThreadLocalChecker dynreg$checker = BlockFixer.BLOCKS_VERSION.threadLocalChecker();

    @Inject(method = "shouldDrawSide", at = @At(value = "FIELD", target = "Lnet/minecraft/block/Block;FACE_CULL_MAP:Ljava/lang/ThreadLocal;"))
    private static void checkCullMapVersion(BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos otherPos, CallbackInfoReturnable<Boolean> cir) {
        if (dynreg$checker.isUpdateNeeded()) {
            FACE_CULL_MAP.remove();
        }
    }
}
