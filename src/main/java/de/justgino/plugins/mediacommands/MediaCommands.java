package de.justgino.plugins.mediacommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class MediaCommands extends JavaPlugin {

    private Texts texts;
    private Media media;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!new File(getDataFolder(), "messages.yml").exists()) saveResource("messages.yml", false);
        texts = new Texts(this, "messages.yml");
        texts.load();
        media = new Media(this);
        getServer().getPluginManager().registerEvents(media, this);
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) new LivePlaceholder(this).register();
        List<String> aliases = getConfig().getStringList("media.aliases");
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands registrar = event.registrar();
            registrar.register(Commands.literal("media")
                    .requires(source -> source.getSender().hasPermission("mediacommands.media"))
                    .executes(ctx -> {
                        Player player = player(ctx);
                        if (player != null) texts.send(player, "media.usage");
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(Commands.argument("link and text", StringArgumentType.greedyString())
                            .suggests((ctx, builder) -> {
                                if (builder.getRemaining().isEmpty()) builder.suggest("https://");
                                return builder.buildFuture();
                            })
                            .executes(ctx -> {
                                Player player = player(ctx);
                                if (player != null) media.post(player, StringArgumentType.getString(ctx, "link and text"));
                                return Command.SINGLE_SUCCESS;
                            })).build(), "Shares a link with everyone", aliases);
            registrar.register(Commands.literal("live")
                    .requires(source -> source.getSender().hasPermission("mediacommands.live"))
                    .executes(ctx -> {
                        Player player = player(ctx);
                        if (player != null) media.toggleLive(player);
                        return Command.SINGLE_SUCCESS;
                    }).build(), "Red dot in front of your name while you stream", List.of());
            registrar.register(Commands.literal("mediacommands")
                    .requires(source -> source.getSender().hasPermission("mediacommands.reload"))
                    .then(Commands.literal("reload").executes(ctx -> {
                        reloadConfig();
                        texts.load();
                        texts.send(ctx.getSource().getSender(), "messages.reloaded");
                        return Command.SINGLE_SUCCESS;
                    })).build(), "MediaCommands admin", List.of());
        });
    }

    private Player player(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getSender() instanceof Player player) return player;
        texts.send(ctx.getSource().getSender(), "messages.players-only");
        return null;
    }

    public Texts texts() {
        return texts;
    }

    public Media media() {
        return media;
    }
}
