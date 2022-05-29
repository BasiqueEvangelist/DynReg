package me.basiqueevangelist.dynreg.testmod;

import me.basiqueevangelist.dynreg.entry.RegistrationEntries;
import me.basiqueevangelist.dynreg.entry.json.EntryDescriptionReaders;
import me.basiqueevangelist.dynreg.testmod.command.CreateCommand;
import me.basiqueevangelist.dynreg.testmod.command.DeleteCommand;
import me.basiqueevangelist.dynreg.testmod.desc.FlowerPotBlockDescription;
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
    }
}
