package de.justgino.plugins.mediacommands;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LivePlaceholder extends PlaceholderExpansion {

    private final MediaCommands plugin;

    public LivePlaceholder(MediaCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mediacommands";
    }

    @Override
    public @NotNull String getAuthor() {
        return "JustGino";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";
        boolean live = plugin.media().isLive(player.getUniqueId());
        return switch (params) {
            case "live" -> live ? LegacyComponentSerializer.legacySection().serialize(plugin.media().marker(player.getUniqueId())) : "";
            case "is_live" -> String.valueOf(live);
            default -> null;
        };
    }
}
