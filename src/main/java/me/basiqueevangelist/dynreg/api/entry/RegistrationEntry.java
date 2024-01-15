package me.basiqueevangelist.dynreg.api.entry;

import me.basiqueevangelist.dynreg.entry.RegistrationEntries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * The registration entry is the indivisible building block of dynamically-registered content.
 * Implementations can register content and declare dependencies on other content.
 */
public interface RegistrationEntry {
    /**
     * Declares registered content and dependencies on other content.
     *
     * <p>Used to top-sort entries.
     *
     * @param ctx scan context
     */
    void scan(EntryScanContext ctx);

    /**
     * Registers content.
     *
     * @param ctx registration context
     */
    void register(EntryRegisterContext ctx);

    /**
     * Registers client-only content (e.g. entity renderers)
     */
    @Environment(EnvType.CLIENT)
    default void registerClient() {

    }

    /**
     * Called when the entry is removed.
     *
     * <p>Should be used to remove content from registries not managed by DynReg.
     */
    default void removed() {

    }

    /**
     * Writes the entry into a buffer.
     * @param buf the output buffer
     */
    void write(PacketByteBuf buf);

    Identifier id();

    /**
     * Returns the entry sent to the player on sync.
     * If nothing is to be synced, return {@code null}.
     *
     * @param player the player to sync with
     * @return the entry that will be sent to the player
     */
    default @Nullable RegistrationEntry toSynced(ServerPlayerEntity player) {
        return this;
    }

    default Identifier typeId() {
        return RegistrationEntries.getEntryId(this);
    }

    /**
     * {@return a hash of the entry, used to calculate the overall hash}
     */
    long hash();
}
