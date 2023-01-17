package me.basiqueevangelist.dynreg.testmod;

import me.basiqueevangelist.dynreg.entry.RegistrationEntries;
import me.basiqueevangelist.dynreg.entry.json.EntryDescriptionReaders;
import me.basiqueevangelist.dynreg.testmod.command.CreateCommand;
import me.basiqueevangelist.dynreg.testmod.command.DeleteCommand;
import me.basiqueevangelist.dynreg.testmod.desc.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;

public class DynRegTest implements ModInitializer {
    public static final String MODID = "dynreg-testmod";

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, dedicated) -> {
            DeleteCommand.register(dispatcher);
            CreateCommand.register(dispatcher);
        });

        RegistrationEntries.registerEntryType(FlowerPotBlockEntry.ID, FlowerPotBlockEntry::new);
        EntryDescriptionReaders.register(FlowerPotBlockEntry.ID, FlowerPotBlockEntry::new);

        RegistrationEntries.registerEntryType(StairsBlockEntry.ID, StairsBlockEntry::new);
        EntryDescriptionReaders.register(StairsBlockEntry.ID, StairsBlockEntry::new);

        RegistrationEntries.registerEntryType(SlabBlockEntry.ID, SlabBlockEntry::new);
        EntryDescriptionReaders.register(SlabBlockEntry.ID, SlabBlockEntry::new);

        RegistrationEntries.registerEntryType(PolymerBlockEntry.ID, PolymerBlockEntry::new);
        EntryDescriptionReaders.register(PolymerBlockEntry.ID, PolymerBlockEntry::new);

        RegistrationEntries.registerEntryType(StatusEffectEntry.ID, StatusEffectEntry::new);
        EntryDescriptionReaders.register(StatusEffectEntry.ID, StatusEffectEntry::new);

        RegistrationEntries.registerEntryType(PotionEntry.ID, PotionEntry::new);
        EntryDescriptionReaders.register(PotionEntry.ID, PotionEntry::new);
    }
}
