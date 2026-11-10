# MediaCommands

`/media` links and a `/live` marker for content creators on Paper and Folia 26.1+ (Java 25).

## Commands

| Command | Permission | |
|---|---|---|
| `/media <link> <text>` (`/ad`) | `mediacommands.media` | One chat line for everyone with an [Open] button and a sound |
| `/live` | `mediacommands.live` | Red dot between prefix and name, on or off |
| `/mediacommands reload` | `mediacommands.reload` | Reloads `config.yml` and `messages.yml` |

`mediacommands.bypass.cooldown` skips the `/media` cooldown. Every node is op by default; give them to your creator rank.

## /live

The dot is put in front of the display name (chat) and the tab list name (`live.display-name`, `live.tab-list`). Chat or tab plugins that build the name themselves can use `%mediacommands_live%` between prefix and name instead. Leaving the server turns it off.

## PlaceholderAPI

| Placeholder | |
|---|---|
| `%mediacommands_live%` | The marker while live, otherwise empty |
| `%mediacommands_is_live%` | `true` / `false` |

## Files

| File | Content |
|---|---|
| `config.yml` | Aliases, cooldown, max length, sounds, where the dot is shown |
| `messages.yml` | Every text, the [Open] button and the dot (MiniMessage) |

## License

MIT, see [LICENSE](LICENSE).
