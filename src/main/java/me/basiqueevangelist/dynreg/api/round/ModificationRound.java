package me.basiqueevangelist.dynreg.api.round;

import me.basiqueevangelist.dynreg.api.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.impl.round.ModificationRoundImpl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

/**
 * A combined and synchronized application of registry entries.
 */
public interface ModificationRound {
    static ModificationRound create(MinecraftServer server) {
        return new ModificationRoundImpl(server);
    }

    ModificationRound entry(RegistrationEntry entry);

    ModificationRound removeEntry(Identifier entryId);

    ModificationRound reloadResourcePacks();

    ModificationRound reloadDataPacks();

    boolean needsRunning();

    CompletableFuture<Void> run();
}
