package me.basiqueevangelist.dynreg.entry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * The registration entry is the indivisible building block of dynamically-registered content.
 * Implementations can register content and declare dependencies on other content.
 */
public interface RegistrationEntry {
    void scan(EntryScanContext ctx);

    void register(EntryRegisterContext ctx);

    @Environment(EnvType.CLIENT)
    default void registerClient() {

    }

    default void onRemoved() {

    }

    void write(PacketByteBuf buf);

    Identifier id();

    /**
     * Returns the entry sent to the player on sync.
     * If nothing is to be synced, return {@code null}.
     *
     * @param player the player to sync with
     * @return the entry that will be sent to the player
     */
    default @Nullable RegistrationEntry toSynced(PlayerEntity player) {
        return this;
    }

    default Identifier typeId() {
        return RegistrationEntries.getEntryId(this);
    }

    int hash();
}
