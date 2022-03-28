package me.basiqueevangelist.dynreg.testmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.basiqueevangelist.dynreg.round.DynamicRound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static net.minecraft.server.command.CommandManager.literal;

public class ApplyCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("dynreg")
                .then(literal("testmod")
                    .then(literal("apply")
                        .executes(ApplyCommand::apply)))
        );
    }

    private static int apply(CommandContext<ServerCommandSource> ctx) {
        DynamicRound round = DynamicRound.getRound(ctx.getSource().getServer());
        round.getRoundEndFuture().thenAccept(unused -> {
            ctx.getSource().sendFeedback(new LiteralText("Applied dynamic changes"), true);
        }).exceptionally(e -> {
            ctx.getSource().sendError(new LiteralText("Failed to apply dynamic changes: " + e));
            return null;
        });

        round.run();

        return 1;
    }
}
