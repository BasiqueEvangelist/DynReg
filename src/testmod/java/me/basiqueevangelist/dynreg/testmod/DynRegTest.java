package me.basiqueevangelist.dynreg.testmod;

import me.basiqueevangelist.dynreg.entry.RegistrationEntries;
import me.basiqueevangelist.dynreg.entry.json.EntryDescriptionReaders;
import me.basiqueevangelist.dynreg.testmod.command.CreateCommand;
import me.basiqueevangelist.dynreg.testmod.command.DeleteCommand;
import me.basiqueevangelist.dynreg.testmod.desc.FlowerPotBlockDescription;
import me.basiqueevangelist.dynreg.testmod.desc.PolymerBlockDescription;
import me.basiqueevangelist.dynreg.testmod.desc.StairsBlockDescription;
import me.basiqueevangelist.dynreg.testmod.desc.StatusEffectDescription;
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

        RegistrationEntries.registerEntryType(FlowerPotBlockDescription.ID, FlowerPotBlockDescription::new);
        EntryDescriptionReaders.register(FlowerPotBlockDescription.ID, FlowerPotBlockDescription::new);

        RegistrationEntries.registerEntryType(StairsBlockDescription.ID, StairsBlockDescription::new);
        EntryDescriptionReaders.register(StairsBlockDescription.ID, StairsBlockDescription::new);

        RegistrationEntries.registerEntryType(PolymerBlockDescription.ID, PolymerBlockDescription::new);
        EntryDescriptionReaders.register(PolymerBlockDescription.ID, PolymerBlockDescription::new);

        RegistrationEntries.registerEntryType(StatusEffectDescription.ID, StatusEffectDescription::new);
        EntryDescriptionReaders.register(StatusEffectDescription.ID, StatusEffectDescription::new);
    }
}
