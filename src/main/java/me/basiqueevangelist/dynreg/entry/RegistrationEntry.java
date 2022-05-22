package me.basiqueevangelist.dynreg.entry;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * The registration entry is the indivisible building block of dynamically-registered content.
 * Implementations can register content and declare dependencies on other content.
 */
public interface RegistrationEntry {
    void scan(EntryScanContext ctx);

    void register(EntryRegisterContext ctx);

    default void onRemoved() {

    }

    void write(PacketByteBuf buf);

    Identifier id();

    default boolean isSynced() {
        return true;
    }

    default Identifier typeId() {
        return RegistrationEntries.getEntryId(this);
    }
}
