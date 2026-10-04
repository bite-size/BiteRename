package gg.bitesize.rename.commands

import gg.bitesize.rename.config.Feature
import gg.bitesize.rename.config.Settings
import gg.bitesize.rename.items.ItemSnapshot
import gg.bitesize.rename.items.ItemText
import gg.bitesize.rename.managers.ConfigManager
import gg.bitesize.rename.managers.FormatManager
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * A `/biterename <name> ...` subcommand. The root command checks player-only,
 * permission, and feature toggles before calling [execute].
 */
abstract class SubCommand(
    val name: String,
    val permission: String,
    /** help.<key> lines shown for this subcommand */
    val helpKeys: List<String> = listOf(name),
    val feature: Feature? = null,
    val playerOnly: Boolean = true,
) {

    protected val settings: Settings get() = ConfigManager.settings

    abstract fun execute(sender: CommandSender, label: String, args: List<String>)

    /** [args] excludes the subcommand name itself. */
    open fun tabComplete(sender: CommandSender, args: List<String>): List<String> = emptyList()

    fun isAvailableTo(sender: CommandSender): Boolean {
        return (!playerOnly || sender is Player)
                && sender.hasPermission(permission)
                && (feature == null || settings.isEnabled(feature))
    }

    /** Sends `errors.<key>`. */
    protected fun error(sender: CommandSender, key: String, vararg placeholders: TagResolver) {
        FormatManager.error(sender, ConfigManager.getMessage("errors.$key"), *placeholders)
    }

    /** Sends `messages.<key>`. */
    protected fun success(sender: CommandSender, key: String, vararg placeholders: TagResolver) {
        FormatManager.send(sender, ConfigManager.getMessage("messages.$key"), true, *placeholders)
    }

    /** Sends `errors.usage-<usageKey>`. */
    protected fun sendUsage(sender: CommandSender, label: String, usageKey: String = name) {
        error(sender, "usage-$usageKey", labelPlaceholder(label))
    }

    protected fun labelPlaceholder(label: String): TagResolver = Placeholder.unparsed("label", label)

    protected fun matching(input: String, options: Iterable<String>): List<String> {
        return options.filter { it.startsWith(input, ignoreCase = true) }
    }

    /** A hint like `<name>` while the argument is still empty. */
    protected fun hint(input: String, hint: String): List<String> {
        return if (input.isEmpty()) listOf(hint) else emptyList()
    }

    /**
     * The player's main-hand item if it can be edited: not air, and not a blacklisted
     * material (unless bypassed). Returns null after messaging the player otherwise.
     */
    protected fun editableItem(player: Player): ItemStack? {
        val item = player.inventory.itemInMainHand

        if (item.type.isAir) {
            error(player, "no-item")
            return null
        }

        if (settings.isMaterialBlacklisted(item.type)) {
            if (!player.hasPermission(Permissions.BYPASS_BLACKLIST)) {
                error(player, "blacklisted-material")
                return null
            }
            success(player, "bypassed-material-blacklist")
        }

        return item
    }

    /** Checks copied or saved text too, so paste and templates can't carry blocked words onto new items. */
    protected fun passesWordBlacklist(player: Player, snapshot: ItemSnapshot): Boolean {
        val text = listOfNotNull(snapshot.displayName) + snapshot.lore.orEmpty()
        val plain = text.joinToString("\n") { ItemText.plainText(FormatManager.fromLegacy(it)) }
        return passesWordBlacklist(player, plain)
    }

    protected fun passesWordBlacklist(player: Player, plain: String): Boolean {
        if (!settings.isWordBlacklisted(plain)) return true

        if (!player.hasPermission(Permissions.BYPASS_BLACKLIST)) {
            error(player, "blacklisted-word")
            return false
        }

        success(player, "bypassed-word-blacklist")
        return true
    }

}
