package me.basiqueevangelist.dynreg.testmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.basiqueevangelist.dynreg.network.block.SimpleBlockDescription;
import me.basiqueevangelist.dynreg.network.item.BlockItemDescription;
import me.basiqueevangelist.dynreg.network.item.SimpleItemDescription;
import me.basiqueevangelist.dynreg.round.DynamicRound;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CreateCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("dynreg")
                .then(literal("testmod")
                    .then(literal("create")
                        .then(literal("item")
                            .then(argument("entry", IdentifierArgumentType.identifier())
                                .executes(CreateCommand::createItem)))
                        .then(literal("block")
                            .then(argument("entry", IdentifierArgumentType.identifier())
                                .executes(CreateCommand::createBlock)))))
        );
    }

    private static int createItem(CommandContext<ServerCommandSource> ctx) {
        DynamicRound round = DynamicRound.getRound(ctx.getSource().getServer());
        Identifier id = IdentifierArgumentType.getIdentifier(ctx, "entry");

        round.addTask(ctx1 -> {
            ctx1.register(id, new SimpleItemDescription(new Item.Settings().group(ItemGroup.MISC)));
            ctx.getSource().sendFeedback(new LiteralText("Created item " + id), false);
        });

        ctx.getSource().sendFeedback(new LiteralText("Added task to current round"), false);
        return 1;
    }

    private static int createBlock(CommandContext<ServerCommandSource> ctx) {
        Identifier id = IdentifierArgumentType.getIdentifier(ctx, "entry");

        DynamicRound round = DynamicRound.getRound(ctx.getSource().getServer());
        round.addTask(ctx1 -> {
            Block block = ctx1.register(id, new SimpleBlockDescription(AbstractBlock.Settings.of(Material.WOOD, MapColor.BLACK)));
            ctx1.register(id, new BlockItemDescription(block, new Item.Settings().group(ItemGroup.MISC)));
            ctx.getSource().sendFeedback(new LiteralText("Created block " + id), false);
        });

        ctx.getSource().sendFeedback(new LiteralText("Added task to current round"), false);

        return 1;
    }
}
