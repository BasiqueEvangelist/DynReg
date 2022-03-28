package me.basiqueevangelist.dynreg.mixin;

import me.basiqueevangelist.dynreg.access.InternalWritable;
import me.basiqueevangelist.dynreg.network.SimpleSerializers;
import me.basiqueevangelist.dynreg.util.NamedEntries;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Rarity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Item.Settings.class)
public class ItemSettingsMixin implements InternalWritable {
    @Shadow int maxCount;

    @Shadow int maxDamage;

    @Shadow @Nullable Item recipeRemainder;

    @Shadow @Nullable ItemGroup group;

    @Shadow Rarity rarity;

    @Shadow @Nullable FoodComponent foodComponent;

    @Shadow boolean fireproof;

    @Override
    public void dynreg$write(PacketByteBuf buf) {
        buf.writeVarInt(maxCount);
        buf.writeVarInt(maxDamage);

        buf.writeBoolean(recipeRemainder != null);
        if (recipeRemainder != null) {
            //noinspection deprecation
            buf.writeIdentifier(recipeRemainder.getRegistryEntry().registryKey().getValue());
        }

        buf.writeString(group == null ? "" : group.getName());
        buf.writeString(NamedEntries.RARITIES.inverse().get(rarity));

        buf.writeBoolean(foodComponent != null);
        if (foodComponent != null) {
            SimpleSerializers.writeFoodComponent(buf, foodComponent);
        }

        buf.writeBoolean(fireproof);
    }
}
