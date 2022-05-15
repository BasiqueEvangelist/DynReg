package me.basiqueevangelist.dynreg.testmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.basiqueevangelist.dynreg.round.DynamicRound;
import me.basiqueevangelist.dynreg.testmod.util.CommandUtils;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DeleteCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("dynreg")
                .then(literal("testmod")
                    .then(literal("remove")
                            .then(argument("entry", IdentifierArgumentType.identifier())
                                .suggests(CommandUtils::suggestEntries)
                                .executes(DeleteCommand::removeEntry))))
        );
    }

    private static int removeEntry(CommandContext<ServerCommandSource> ctx) {
        Identifier entryId = IdentifierArgumentType.getIdentifier(ctx, "entry");

        DynamicRound round = new DynamicRound(ctx.getSource().getServer());
        round.removeEntry(entryId);
        round.run();

        ctx.getSource().sendFeedback(new LiteralText("Removed " + entryId), true);

        return 0;
    }
}
