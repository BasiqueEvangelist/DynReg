package me.basiqueevangelist.dynreg.testmod;

import me.basiqueevangelist.dynreg.testmod.command.ApplyCommand;
import me.basiqueevangelist.dynreg.testmod.command.CreateCommand;
import me.basiqueevangelist.dynreg.testmod.command.DeleteCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.util.registry.RegistryKey;

import java.util.HashSet;
import java.util.Set;

public class DynRegTest implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            DeleteCommand.register(dispatcher);
            CreateCommand.register(dispatcher);
            ApplyCommand.register(dispatcher);
        });
    }
}
