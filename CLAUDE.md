# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Build and run

```bash
./gradlew build       # shaded plugin jar: build/libs/BiteRename-<version>.jar (ignore the -plain jar)
./gradlew runServer   # Paper 26.3 in run/ (the user's own test server)
./gradlew runPurpur   # Purpur 26.3 in run-purpur/ (downloads .servers/purpur-26.3.jar)
./gradlew runSpigot   # Spigot 26.3 in run-spigot/ (first run builds .servers/spigot-26.3.jar with BuildTools, ~5 min)
```

- All three tasks load the current shadow jar. `testServerVersion` in build.gradle.kts sets the Minecraft version for all of them.
- Pass `-PrefreshServers` to re-download Purpur or rebuild Spigot. Server jars are cached in `.servers/` (gitignored), as are `run-purpur/` and `run-spigot/`.
- `runSpigot` uses `legacyPluginLoading()`, because Spigot has no `-add-plugin` flag.

There are no unit tests. Verify on a real server, on all three platforms before a release (see "In-game testing").

## Platform rules

- **Must run on Spigot, Paper, and Purpur 26.3.** Compile against `spigot-api` only. Don't use Paper-only API: no `ItemStack.editMeta` (use `items/ItemEditor.kt`'s `edit {}`), no `Server.getCommandMap()`, and no sending Adventure components to a `CommandSender`.
- Java 25 toolchain, Kotlin 2.4. The Kotlin stdlib is **not** shaded. It loads through plugin.yml `libraries` (`kotlin.stdlib.default.dependency=false`, `compileOnly(kotlin("stdlib"))`).
- Shaded and relocated: Adventure MiniMessage plus the legacy and plain serializers (`gg.bitesize.rename.libs.kyori`) and bStats (`gg.bitesize.rename.libs.bstats`, plugin ID 34507).
- Vault and PlaceholderAPI are `compileOnly` soft dependencies. Only touch their classes after checking `VaultManager.isInstalled` or `PapiManager.isInstalled`.
- `processResources` expands `${version}` and `${kotlinVersion}` in plugin.yml. When releasing, keep `version` in build.gradle.kts and `version:` in config.yml in sync.

## Architecture (`gg.bitesize.rename`)

| Package | Contents |
|---|---|
| `BiteRename.kt` | Main class: load config, then templates, then commands, listener, first-tick economy check, bStats |
| `commands/` | `RenameCommand` (root `/biterename`, alias `/br`), the `SubCommand` and `ItemSubCommand` bases, `Permissions` |
| `commands/sub/` | One class per subcommand (rename, lore, clear, hide/unhide, copy/paste, template(s), reload) |
| `config/Settings.kt` | Immutable snapshot of config.yml, rebuilt on load and reload. Feature and cost keys are enums |
| `items/` | `ItemText` (player text pipeline), `ItemSnapshot` (name, lore, flags), `ItemEditor` (cross-platform ItemStack edits) |
| `managers/` | `ConfigManager`, `FormatManager`, `CommandManager`, `ClipboardManager`, `TemplateManager`, `EconomyManager`, `VaultManager`, `PapiManager`, `CooldownManager` (template code, unused) |
| `listeners/` | `PlayerListener` clears clipboards on quit |

### Commands

- Registered dynamically through the command map, which `CommandManager` resolves reflectively because Spigot doesn't expose it.
- Item edits follow one order: held item → material blacklist → validation → `EconomyManager.charge` → apply. Never charge before every check has passed.
- Help and tab completion only list subcommands where `SubCommand.isAvailableTo(sender)` is true.
- **To add a subcommand:**
  - write the class in `commands/sub/` and add it to the list in `RenameCommand`
  - add `errors.usage-<name>` and `help.<key>` to messages.yml
  - add the permission to `Permissions` and plugin.yml

### Text pipeline quirks

- **Output:** all chat and item text is written as section-sign legacy text with hex colors (`FormatManager.toLegacy`). That's what works the same on every platform and on the console.
- **Player text** goes through `ItemText`:
  1. `§` becomes `&`.
  2. `&` codes and `&#hex` become MiniMessage tags.
  3. MiniMessage parses it with visual tags only.
  4. Blacklist and length checks use the plain text.
- **Upright text:** a leading `§r` makes CraftBukkit store `italic:false`. Legacy getters (`getLore`, `getDisplayName`) drop that, so any code that writes existing lines back must call `ItemText.upright(...)` again, or those lines turn italic.
- **Reading YAML that users can edit:** check `isString` or `isList` first. `getString()` on a nested section returns the section's `toString()`.

### Storage

- **templates.yml:** `<uuid>.<template>.{name, lore, flags}`, the same layout as LRename. `TemplateManager` keeps it in memory. Writes are batched about 2s, built on the main thread, and written asynchronously through a temp file and atomic move, with a final synchronous save on disable. On first start it imports `plugins/LRename/templates.yml` if present.
- **Clipboards:** memory only, cleared when the player quits.

## Messages and branding

- messages.yml mirrors BiteMenus:
  - the banner header
  - `prefix: '<#FF0368>[BiteRename] '` (not bold) and `error-prefix: '<#F7567C>!! '`
  - `errors`, `messages`, and `help` sections
  - inline hex colors: errors `#F7567C`, successes `#4ECDC4`, values `#4ECDC4`/`#F7FFF7`, help lines `<gray>- /<label> ...`
- Message keys missing from a server's file fall back to the bundled messages.yml, so new keys work without regenerating.
- Palette: Magenta Bloom `#FF0368`, Strong Cyan `#4ECDC4`, Ink Black `#011627`, Mint Cream `#F7FFF7`, Bubblegum Pink `#F7567C`. Voice: friendly, concise, confident.

## In-game testing

- **Before any run task, or before touching anything in `run/`, check that no server is already running** (a `java.exe` with `run-task=true`, or port 25565 in use). The user runs their own test server in `run/`, and a second launch re-patches the shared Paper jar under it. All three platforms use port 25565, so run them one at a time.
- Prefer `run-purpur/` and `run-spigot/` for automated tests, and leave `run/` to the user.
- A player can be simulated with a mineflayer bot (26.1 client):
  - Add ViaVersion and ViaBackwards to `run/plugins` temporarily.
  - Set `online-mode=false`, `enforce-secure-profile=false`, and `white-list=false`.
  - Have the bot drop its movement packets, or Paper kicks it.
  - Feed console commands through `tail -f <file> | ./gradlew <run task>`. Detach `tail`'s stdio, and stop it afterwards, because it never exits by itself.
  - In Git Bash, `$TMP` is the system temp folder, not the session scratchpad.
- **Ops bypass costs by default.** To test costs, add LuckPerms and negate `biterename.bypass.cost`. EssentialsX with VaultUnlocked works as a Vault economy on 26.3. On Spigot, EssentialsX 2.22.0 logs a harmless `NoSuchMethodException` from its own reflection.
- **Revert everything afterwards:**
  - `server.properties`
  - added plugins and their data folders, including `faststats` from VaultUnlocked
  - test configs
  - `run/ops.json`, which is tracked
  - Leave files the user created (for example their `templates.yml`).

## Conventions

- Commit subjects: `base: <short summary> (v<version>)`.
- Resource files use CRLF. `sed -i` in Git Bash strips CRLF, so edit them with the Edit tool or restore the endings afterwards.
- Follow `~/.claude/CLAUDE.md`: author `bitesize`, prefer Kotlin, and ask before destructive actions.
