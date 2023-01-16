package me.basiqueevangelist.dynreg.holder;

import me.basiqueevangelist.dynreg.entry.RegistrationEntry;

import java.util.function.Consumer;

public class EntryHasher implements Consumer<RegistrationEntry> {
    private long hash = 0;

    @Override
    public void accept(RegistrationEntry entry) {
        long entryHash = ((long) entry.typeId().hashCode() << 32) | entry.hash();

        hash += entryHash;
    }

    public void remove(RegistrationEntry entry) {
        long entryHash = ((long) entry.typeId().hashCode() << 32) | entry.hash();

        hash -= entryHash;
    }

    public long hash() {
        return hash;
    }
}
