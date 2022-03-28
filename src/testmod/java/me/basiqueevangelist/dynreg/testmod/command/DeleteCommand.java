package me.basiqueevangelist.dynreg.testmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.basiqueevangelist.dynreg.access.ExtendedRegistry;
import me.basiqueevangelist.dynreg.network.block.SimpleBlockDescription;
import me.basiqueevangelist.dynreg.round.DynamicRound;
import me.basiqueevangelist.dynreg.testmod.util.CommandUtils;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DeleteCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("dynreg")
                .then(literal("testmod")
                    .then(literal("remove")
                        .then(argument("registry", RegistryKeyArgumentType.registryKey(RegistryKey.ofRegistry(new Identifier("root"))))
                            .suggests(CommandUtils::suggestRegistries)
                            .then(argument("entry", IdentifierArgumentType.identifier())
                                .suggests(CommandUtils::suggestEntriesOfRegistry)
                                .executes(DeleteCommand::removeEntry)))))
        );
    }

    private static int removeEntry(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        RegistryEntry.Reference<?> entry = CommandUtils.getEntry(ctx);
        RegistryKey<?> key = entry.registryKey();

        DynamicRound round = DynamicRound.getRound(ctx.getSource().getServer());
        round.addTask(ctx1 -> {
            ctx1.removeEntry(entry.registry, key.getValue());
        });
        ctx.getSource().sendFeedback(new LiteralText("Removed " + key), true);

        return 0;
    }
}
