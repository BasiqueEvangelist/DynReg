package me.basiqueevangelist.dynreg.mixin.fabriccommon;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiLookupMap;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap;
import net.fabricmc.fabric.impl.lookup.block.BlockApiLookupImpl;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BlockApiLookupImpl.class, remap = false)
public interface BlockApiLookupImplAccessor {
    @Accessor
    static ApiLookupMap<BlockApiLookup<?, ?>> getLOOKUPS() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    ApiProviderMap<Block, BlockApiLookup.BlockApiProvider<?, ?>> getProviderMap();
}
