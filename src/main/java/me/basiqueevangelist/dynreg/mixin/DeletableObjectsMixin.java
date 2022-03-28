package me.basiqueevangelist.dynreg.mixin;

import me.basiqueevangelist.dynreg.access.DeletableObject;
import me.basiqueevangelist.dynreg.access.DeletableObjectInternal;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.util.registry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({RegistryEntry.Reference.class, Block.class, Item.class, BlockEntityType.class})
public class DeletableObjectsMixin implements DeletableObjectInternal {
    private boolean dynreg$deleted = false;

    @Override
    public void markAsDeleted() {
        dynreg$deleted = true;
    }

    @Override
    public boolean wasDeleted() {
        return dynreg$deleted;
    }
}
