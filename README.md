<div align="center">

# BiteRename

**Rename items, write lore, and save reusable templates, all in-game.**

[![Minecraft 26.3](https://img.shields.io/badge/Minecraft-26.3-FF0368?style=flat-square)](#compatibility)
[![Spigot | Paper | Purpur](https://img.shields.io/badge/Spigot%20%7C%20Paper%20%7C%20Purpur-supported-4ECDC4?style=flat-square)](#compatibility)
[![Java 25](https://img.shields.io/badge/Java-25-7209B7?style=flat-square)](#building-from-source)
[![Latest release](https://img.shields.io/github/v/release/bite-size/BiteRename?style=flat-square&color=FF0368&label=release)](https://github.com/bite-size/BiteRename/releases)
[![bStats servers](https://img.shields.io/bstats/servers/34507?style=flat-square&color=4ECDC4&label=servers)](https://bstats.org/plugin/bukkit/BiteRename/34507)

</div>

---

BiteRename is a lightweight item editor for survival and creative servers. Players style names and lore with MiniMessage or classic `&` codes, copy a look from one item to another, and save their favorites as templates. Admins stay in control with feature toggles, blacklists, limits, and optional Vault or XP costs.

```text
/br rename <gradient:#FF0368:#4ECDC4>Bitesize Blade</gradient>
/br lore add &7Forged for &#4ECDC4champions
/br hide attributes
/br template save blade
```

## Highlights

- **Modern formatting.** MiniMessage gradients, hex colors, and legacy `&` codes, even mixed in one line. Names and lore show upright instead of in vanilla italics.
- **Copy, paste, and templates.** Copy an item's name, lore, and hidden flags, paste them onto another item, or save them as named templates that persist across restarts.
- **Smart tab completion.** Suggests subcommands, your template names, and the actual lore line numbers on the item you're holding.
- **Economy ready.** Charge money through Vault or XP levels per action. Players are only charged once every check has passed.
- **Safe by default.** Word and item blacklists that see through formatting tricks, per-feature toggles, length limits, and permission-gated placeholders.
- **Lightweight.** No extra libraries to install, nothing heavy on the main thread, and template saves are written in the background.
- **Coming from LRename?** Your saved templates are imported automatically on first start.

## Getting started

1. Download the latest jar from [Releases](https://github.com/bite-size/BiteRename/releases).
2. Drop it into your server's `plugins/` folder.
3. Restart the server.
4. *(Optional)* Install [Vault](https://www.spigotmc.org/resources/vault.34315/) with an economy plugin for money costs, and [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) for placeholders in item text.

> [!NOTE]
> On its first start, the server downloads the Kotlin standard library from Maven Central, so it needs internet access once.

## Commands

The main command is `/biterename`, with the shorter alias `/br`. Everything works on the item in your main hand.

| Command | What it does | Permission |
|---|---|---|
| `/br help` | Shows the commands you can use | `biterename.use` |
| `/br rename <name>` | Renames your item | `biterename.rename` |
| `/br lore add <text>` | Adds a lore line | `biterename.lore` |
| `/br lore delline <line>` | Removes a lore line by number | `biterename.lore` |
| `/br clear [name\|lore]` | Clears the name, the lore, or both | `biterename.clear` |
| `/br hide <enchants\|attributes>` | Hides enchantments or attributes | `biterename.hide` |
| `/br unhide <enchants\|attributes>` | Shows them again | `biterename.hide` |
| `/br copy` | Copies the name, lore, and flags | `biterename.copypaste` |
| `/br paste` | Pastes your clipboard onto your item | `biterename.copypaste` |
| `/br template save <name>` | Saves your item's look as a template | `biterename.templates` |
| `/br template load <name>` | Applies a template to your item | `biterename.templates` |
| `/br template delete <name>` | Deletes a template | `biterename.templates` |
| `/br templates` | Lists your templates | `biterename.templates` |
| `/br reload` | Reloads `config.yml` and `messages.yml` | `biterename.admin` |

### Extra permissions

| Permission | Effect |
|---|---|
| `biterename.*` | All BiteRename permissions, including the bypasses |
| `biterename.bypass.cost` | Never charged for actions |
| `biterename.bypass.blacklist` | Can use blocked words and edit blocked items (with a notice) |
| `biterename.placeholders` | PlaceholderAPI placeholders are filled in for this player's text |

All permissions default to **op**. Players without `biterename.use` won't see the command at all.

> [!TIP]
> Since operators have `biterename.bypass.cost`, they're never charged. To test costs as an op, negate that permission with your permissions plugin.

## Formatting

Any name or lore line can use:

| Style | Example |
|---|---|
| Named colors | `<red>Ruby`, `<gold>Gilded` |
| Hex colors | `<#FF0368>Magenta` or `&#FF0368Magenta` |
| Gradients and rainbows | `<gradient:#FF0368:#4ECDC4>Bitesize</gradient>`, `<rainbow>Prism</rainbow>` |
| Decorations | `<bold>`, `<italic>`, `<underlined>`, `<strikethrough>`, `<obfuscated>` |
| Legacy codes | `&c&lBold Red`, `&x&F&F&0&3&6&8Spigot hex` |

Need some breathing room in your lore? `/br lore add &r` (or any formatting with no text, like `<red>`) adds a blank spacer line. Server owners can turn this off with `allow-empty-lore-lines`.

Only visual tags are allowed. Interactive or server-side tags like `<click>`, `<hover>`, `<selector>`, or `<nbt>` stay as plain text, so players can't sneak them onto items.

With PlaceholderAPI installed, players with `biterename.placeholders` can write placeholders like `%player_name%` into their text. They're filled in once, when the text is applied.

## Configuration

<details>
<summary><b>config.yml</b>: features, limits, blacklists, and costs</summary>

```yaml
# Turn whole features on or off
features:
  rename: true
  lore: true
  clear: true
  hide: true
  clipboard: true    # /br copy and /br paste
  templates: true

# Lengths count visible characters, so formatting doesn't count. 0 means no limit.
limits:
  max-lore-lines: 10
  max-name-length: 0
  max-lore-length: 0
  max-templates: 0   # per player

formatting:
  # Remove the vanilla italics from custom names and lore
  upright-text: true
  # Let players add blank lore lines to space out lore, e.g. /br lore add &r
  allow-empty-lore-lines: true

blacklists:
  # Blocked anywhere in a name or lore line, ignoring case and formatting
  words:
    - "admin"
    - "owner"
    - "staff"
  # Items that can't be edited at all
  materials:
    - "BEDROCK"
    - "BARRIER"
    - "COMMAND_BLOCK"

economy:
  # NONE, VAULT (needs Vault and an economy plugin), or XP (charges levels)
  type: "NONE"
  # Cost per action. 0 makes it free.
  costs:
    rename: 0.0
    lore-add: 0.0
    lore-delline: 0.0
    clear-all: 0.0
    clear-name: 0.0
    clear-lore: 0.0
    hide: 0.0
    paste: 0.0
    template-load: 0.0
```

With `type: "XP"`, costs are whole levels. With `type: "VAULT"`, BiteRename tells you in the console if Vault or an economy plugin is missing.

</details>

<details>
<summary><b>messages.yml</b>: every message the plugin sends</summary>

Messages are grouped into `errors`, `messages`, and `help`, and all of them support MiniMessage. The `prefix` and `error-prefix` at the top are added for you.

| Variable | Meaning |
|---|---|
| `<label>` | The command label the player used, like `br` |
| `<max>` | A configured limit (characters, lore lines, or templates) |
| `<line>` | A lore line number |
| `<cost>` | The cost of an action, like `$100` or `5` |
| `<template>` | A template name |
| `<templates>` | The player's template list |

New messages from updates are filled in automatically, so you never need to regenerate the file.

</details>

<details>
<summary><b>templates.yml</b>: saved templates</summary>

Templates are stored per player UUID. BiteRename keeps them in memory and writes this file in the background, so edit it only while the server is stopped.

If `plugins/LRename/templates.yml` exists the first time BiteRename starts, its templates are imported automatically.

</details>

## Compatibility

| | |
|---|---|
| **Minecraft** | 26.3 |
| **Servers** | Spigot, Paper, and Purpur |
| **Java** | 25 |
| **Optional** | Vault (or VaultUnlocked) with an economy plugin, PlaceholderAPI |

## Building from source

```bash
git clone https://github.com/bite-size/BiteRename.git
cd BiteRename
./gradlew build
```

The plugin jar is written to `build/libs/BiteRename-<version>.jar`.

To try it on a local test server with the freshly built plugin loaded:

| Platform | Command | Folder |
|---|---|---|
| Paper | `./gradlew runServer` | `run/` |
| Purpur | `./gradlew runPurpur` | `run-purpur/` |
| Spigot | `./gradlew runSpigot` | `run-spigot/` (the first run builds Spigot with BuildTools, which takes a few minutes) |

Server jars are cached in `.servers/`. Add `-PrefreshServers` to fetch the latest builds.
