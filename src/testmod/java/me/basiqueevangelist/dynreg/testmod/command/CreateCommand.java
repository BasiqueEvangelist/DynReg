package me.basiqueevangelist.dynreg.testmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.basiqueevangelist.dynreg.network.block.SimpleBlockDescription;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
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
import net.minecraft.util.registry.Registry;
import me.basiqueevangelist.dynreg.round.DynamicRound;

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
        RegistryUtils.unfreeze(Registry.ITEM);
        Item item = new Item(new Item.Settings().group(ItemGroup.MISC));
        Identifier id = IdentifierArgumentType.getIdentifier(ctx, "entry");
        Registry.register(Registry.ITEM, id, item);
        Registry.ITEM.freeze();

        ctx.getSource().sendFeedback(new LiteralText("Created item " + id), true);
        return 1;
    }

    private static int createBlock(CommandContext<ServerCommandSource> ctx) {
        Identifier id = IdentifierArgumentType.getIdentifier(ctx, "entry");

        DynamicRound round = DynamicRound.getRound(ctx.getSource().getServer());
        round.addTask(ctx1 -> {
            SimpleBlockDescription blockDesc = new SimpleBlockDescription(AbstractBlock.Settings.of(Material.WOOD, MapColor.BLACK));

            ctx1.register(id, blockDesc);
        });
        ctx.getSource().sendFeedback(new LiteralText("Created block " + id), true);

        return 1;
    }
}
