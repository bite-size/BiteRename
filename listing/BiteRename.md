# BiteRename

*Rename items, write lore, and save the look for later, all in-game.*

## ▸ Overview

Anvils give you one plain name and no lore. BiteRename gives your players gradients, hex colors, and multi-line lore with a single command. When they've styled the perfect item, they can copy that look onto another item or save it as a template to reuse whenever they like. Admins stay in control with feature toggles, blacklists, limits, and optional Vault or XP costs.

## ▸ Key Features

- **Modern Formatting:** MiniMessage gradients, HEX colors, and classic `&` codes, even mixed in the same line.
- **Copy & Paste:** Copy an item's name, lore, and hidden flags, then paste them onto another item.
- **Persistent Templates:** Save your favorite looks by name and apply them any time, even after a restart.
- **API Integrations:** Full Vault support for economy costs and PlaceholderAPI support for live variables in names and lore.
- **Spigot, Paper & Purpur:** One jar that runs on all three, tested on each.

## ▸ Formatting

Every name and lore line understands both MiniMessage and legacy codes. Names and lore show upright, without the vanilla italics.

| Example | Result |
|---|---|
| `<red>Ruby`, `<gold>Gilded` | Named colors |
| `<#FF0368>Magenta`, `&#FF0368Magenta` | HEX colors |
| `<gradient:#FF0368:#4ECDC4>Bitesize</gradient>` | Smooth gradients (rainbows too) |
| `<bold>`, `<italic>`, `<underlined>`, `<strikethrough>` | Decorations |
| `&c&lBold Red` | Classic legacy codes |
| `/br lore add &r` | A blank lore line to space things out |

Only visual tags are allowed, so players can't sneak click, hover, or selector tags onto items.

## ▸ Additional Features

- Every single message is completely configurable in the `messages.yml`.
- Smart TAB completion suggests subcommands, your saved templates, and the exact lore lines on the item you're holding.
- Word and item blacklists that can't be dodged with color codes, plus length and lore line limits.
- Turn any feature on or off: renaming, lore, clearing, hiding flags, copy/paste, and templates.
- Players are only charged once every check has passed, so nobody pays for a failed edit.
- Lightweight: nothing extra to install, and template saves are written in the background.

## ▸ Item Commands

| Command | Description | Permission |
|---|---|---|
| `/br rename <name>` | Rename the held item | `biterename.rename` |
| `/br lore add <text>` | Add a lore line | `biterename.lore` |
| `/br lore delline <line>` | Remove a lore line | `biterename.lore` |
| `/br clear [name\|lore]` | Clear the name, lore, or both | `biterename.clear` |
| `/br hide <enchants\|attributes>` | Hide enchantments or attributes | `biterename.hide` |
| `/br unhide <enchants\|attributes>` | Show them again | `biterename.hide` |
| `/br copy` | Copy the name, lore, and flags | `biterename.copypaste` |
| `/br paste` | Paste your clipboard onto an item | `biterename.copypaste` |
| `/br help` | Show the help menu | `biterename.use` |

## ▸ Template Commands

| Command | Description | Permission |
|---|---|---|
| `/br template save <name>` | Save your item's look as a template | `biterename.templates` |
| `/br template load <name>` | Apply a template to your item | `biterename.templates` |
| `/br template delete <name>` | Delete a template | `biterename.templates` |
| `/br templates` | List your templates | `biterename.templates` |

## ▸ Admin Commands & Permissions

| Command / Permission | Description |
|---|---|
| `/br reload` | Reload config & messages (`biterename.admin`) |
| `biterename.bypass.cost` | Never charged for actions |
| `biterename.bypass.blacklist` | Can use blocked words and edit blocked items |
| `biterename.placeholders` | PlaceholderAPI placeholders are filled in for this player's text |
| `biterename.*` | Every BiteRename permission |

All permissions default to op. *Every command also works as `/biterename`.*

## ▸ Configuration

- **Features:** Toggle renaming, lore, clearing, hiding, copy/paste, and templates individually.
- **Limits:** Max lore lines, name and lore length (formatting doesn't count), and templates per player.
- **Formatting:** Upright (non-italic) text and blank lore lines, each toggleable.
- **Blacklists:** Blocked words (case and formatting ignored) and items that can't be edited.
- **Economy:** `NONE`, `VAULT`, or `XP`, with a separate cost for each action.

<details>
<summary><b>config.yml</b></summary>

```yaml
features:
  rename: true
  lore: true
  clear: true
  hide: true
  clipboard: true    # /br copy and /br paste
  templates: true

limits:
  max-lore-lines: 10
  max-name-length: 0
  max-lore-length: 0
  max-templates: 0   # per player

formatting:
  upright-text: true
  allow-empty-lore-lines: true

blacklists:
  words:
    - "admin"
    - "owner"
    - "staff"
  materials:
    - "BEDROCK"
    - "BARRIER"
    - "COMMAND_BLOCK"

economy:
  type: "NONE"       # NONE, VAULT, or XP
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

</details>

## ▸ Messages

Every message lives in `messages.yml`, grouped into `errors`, `messages`, and `help`, with full MiniMessage support. Set your own prefix and error prefix, and use these variables:

| Variable | Meaning |
|---|---|
| `<label>` | The command label used (e.g. `br`) |
| `<max>` | A configured limit |
| `<line>` | A lore line number |
| `<cost>` | The cost of an action (e.g. `$100` or `5`) |
| `<template>` / `<templates>` | A template name / the player's template list |

New messages added in updates are filled in automatically, so you never have to regenerate the file.

## ▸ Soft Dependencies

- **Vault API:** Needed if you want to charge money for edits (works with Vault or VaultUnlocked plus any economy plugin).
- **PlaceholderAPI:** Needed if you want variables (`%player_name%`, etc.) in names and lore.

## ▸ Compatibility

- **Minecraft:** 26.3
- **Servers:** Spigot, Paper, and Purpur
- **Java:** 25

*On first start, the server downloads the Kotlin standard library once, so it needs internet access.*

## ▸ Coming from LRename?

BiteRename is the successor to LRename. Drop it in and your players' saved templates are imported automatically on the first start.

## ▸ Video Tutorial

Coming soon...

## ▸ Support

Lost, confused, or stuck at any point? Reach out to me here, on GitHub, or on Discord (username: **.bite.size**).
