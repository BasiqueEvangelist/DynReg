package me.basiqueevangelist.dynreg.testmod.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;

import java.util.concurrent.CompletableFuture;

public final class CommandUtils {
    private static final DynamicCommandExceptionType NO_SUCH_REGISTRY = new DynamicCommandExceptionType(name -> new LiteralText(name + " is not a valid registry!"));
    private static final Dynamic2CommandExceptionType NO_SUCH_ENTRY = new Dynamic2CommandExceptionType((registry, entry) -> new LiteralText(entry + " is not an entry of " + registry + "!"));

    private CommandUtils() {

    }


    @SuppressWarnings("unchecked")
    public static Registry<?> getRegistry(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        RegistryKey<?> key = RegistryKeyArgumentType.getKey(ctx, "registry", (RegistryKey<Registry<Object>>)(Object) Registry.REGISTRIES.getKey(), NO_SUCH_REGISTRY);

        return Registry.REGISTRIES.getOrEmpty(key.getValue()).orElseThrow(() -> NO_SUCH_REGISTRY.create(key.getValue()));
    }

    @SuppressWarnings("unchecked")
    public static RegistryEntry.Reference<?> getEntry(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Registry<?> registry = getRegistry(ctx);
        Identifier id = IdentifierArgumentType.getIdentifier(ctx, "entry");
        return (RegistryEntry.Reference<?>) ((Registry<Object>) registry).getEntry((RegistryKey<Object>) RegistryKey.of(registry.getKey(), id)).orElseThrow(() -> NO_SUCH_ENTRY.create(registry.getKey().getValue(), id));
    }

    public static CompletableFuture<Suggestions> suggestRegistries(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        for (Identifier id : Registry.REGISTRIES.getIds()) {
            builder.suggest(String.valueOf(id));
        }

        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestEntriesOfRegistry(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) throws CommandSyntaxException {
        Registry<?> registry = getRegistry(ctx);

        for (Identifier id : registry.getIds()) {
            builder.suggest(String.valueOf(id));
        }

        return builder.buildFuture();
    }
}
