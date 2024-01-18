package me.basiqueevangelist.dynreg.mixin;

import me.basiqueevangelist.dynreg.impl.access.DeletableObjectInternal;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({RegistryEntry.Reference.class, Block.class, Item.class, BlockEntityType.class, StatusEffect.class, EntityType.class})
public class DeletableObjectsMixin implements DeletableObjectInternal {
    private boolean dynreg$deleted = false;
    private Identifier dynreg$id = null;

    @Override
    public void dynreg$setDeleted(boolean value) {
        dynreg$deleted = value;
    }

    @Override
    public void dynreg$setId(Identifier id) {
        dynreg$id = id;
    }

    @Override
    public Identifier dynreg$getId() {
        return dynreg$id;
    }

    @Override
    public boolean wasDeleted() {
        return dynreg$deleted;
    }
}
