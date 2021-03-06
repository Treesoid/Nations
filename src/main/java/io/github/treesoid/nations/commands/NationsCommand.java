package io.github.treesoid.nations.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.treesoid.nations.config.NationsServerConfig;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

import static net.minecraft.server.command.CommandManager.literal;

public class NationsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("nations").requires(source -> source.hasPermissionLevel(3))
                .then(literal("reload").executes(NationsCommand::reloadConfig))
        );
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> ctx) {
        NationsServerConfig.load();
        ctx.getSource().sendFeedback(new TranslatableText("nations.commands.nations.reloaded"), true);
        return 1;
    }
}
