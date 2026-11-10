package de.justgino.plugins.mediacommands;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Media implements Listener {

    private record Names(Component display, Component list) {
    }

    private final MediaCommands plugin;
    private final Map<UUID, Names> live = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public Media(MediaCommands plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration config() {
        return plugin.getConfig();
    }

    private Texts texts() {
        return plugin.texts();
    }

    public boolean isLive(UUID player) {
        return live.containsKey(player);
    }

    public void post(Player player, String input) {
        int space = input.indexOf(' ');
        if (input.isBlank() || space < 0) {
            texts().send(player, "media.usage");
            return;
        }
        URI link = link(input.substring(0, space));
        if (link == null) {
            texts().send(player, "media.invalid-link");
            return;
        }
        String text = input.substring(space + 1).trim();
        int max = Math.max(1, config().getInt("media.max-length", 150));
        if (text.isEmpty() || text.length() > max) {
            texts().send(player, "media.too-long", "max", max);
            return;
        }
        long left = cooldownLeft(player);
        if (left > 0) {
            texts().send(player, "media.cooldown", "time", texts().duration(left));
            return;
        }
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + Math.max(0, config().getLong("media.cooldown", 1800)) * 1000L);
        String url = link.toString();
        Component open = texts().get("media.open")
                .clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(HoverEvent.showText(texts().get("media.open-hover", "link", url, "player", player.getName())));
        Component line = texts().get("media.message", "open", open, "text", text, "player", player.getName());
        Sound sound = sound("media");
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.getScheduler().run(plugin, task -> {
                online.sendMessage(line);
                if (sound != null) online.playSound(sound);
            }, null);
        }
        Bukkit.getConsoleSender().sendMessage(line);
    }

    private long cooldownLeft(Player player) {
        if (player.hasPermission("mediacommands.bypass.cooldown")) return 0;
        Long until = cooldowns.get(player.getUniqueId());
        long left = until == null ? 0 : until - System.currentTimeMillis();
        return left > 0 ? (left + 999) / 1000 : 0;
    }

    private static URI link(String input) {
        try {
            URI uri = new URI(input.trim());
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!scheme.equals("http") && !scheme.equals("https")) return null;
            if (uri.getHost() == null || uri.getHost().isBlank()) return null;
            return uri;
        } catch (Exception e) {
            return null;
        }
    }

    public void toggleLive(Player player) {
        UUID uuid = player.getUniqueId();
        Names original = live.remove(uuid);
        if (original != null) {
            restore(player, original);
            texts().send(player, "live.off");
        } else {
            live.put(uuid, new Names(player.displayName(), player.playerListName()));
            apply(player);
            texts().send(player, "live.on");
        }
        Sound sound = sound("live");
        if (sound != null) player.playSound(sound);
    }

    private void apply(Player player) {
        Component marker = texts().get("live.marker");
        Names original = live.get(player.getUniqueId());
        if (original == null) return;
        if (config().getBoolean("live.display-name", true)) player.displayName(marker.append(original.display()));
        if (config().getBoolean("live.tab-list", true)) player.playerListName(marker.append(original.list()));
    }

    private void restore(Player player, Names original) {
        if (config().getBoolean("live.display-name", true)) player.displayName(original.display());
        if (config().getBoolean("live.tab-list", true)) player.playerListName(original.list());
    }

    public Component marker(UUID player) {
        return isLive(player) ? texts().get("live.marker") : Component.empty();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Names original = live.remove(event.getPlayer().getUniqueId());
        if (original != null) restore(event.getPlayer(), original);
    }

    private Sound sound(String section) {
        String name = config().getString(section + ".sound", "");
        if (name == null || name.isBlank()) return null;
        try {
            return Sound.sound(Key.key(name.trim().toLowerCase(Locale.ROOT)), Sound.Source.MASTER,
                    (float) config().getDouble(section + ".volume", 1.0), (float) config().getDouble(section + ".pitch", 1.0));
        } catch (Exception e) {
            plugin.getLogger().warning("Unknown sound in config.yml: " + name);
            return null;
        }
    }
}
