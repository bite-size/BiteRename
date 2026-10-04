package gg.bitesize.rename.commands

import gg.bitesize.rename.config.Feature
import gg.bitesize.rename.config.Settings
import gg.bitesize.rename.managers.ConfigManager
import gg.bitesize.rename.managers.FormatManager
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * A `/biterename <name> ...` subcommand. The root command checks player-only,
 * permission, and feature toggles before calling [execute].
 */
abstract class SubCommand(
    val name: String,
    val permission: String,
    private val usageArgs: String = "",
    /** help.entries keys shown for this subcommand */
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

    protected fun error(sender: CommandSender, key: String, vararg placeholders: TagResolver) {
        FormatManager.error(sender, ConfigManager.getMessage(key), *placeholders)
    }

    protected fun success(sender: CommandSender, key: String, vararg placeholders: TagResolver) {
        FormatManager.send(sender, ConfigManager.getMessage(key), true, *placeholders)
    }

    protected fun sendUsage(sender: CommandSender, label: String, args: String = usageArgs) {
        error(sender, "usage", Placeholder.unparsed("usage", "/$label $name $args".trimEnd()))
    }

    protected fun matching(input: String, options: Iterable<String>): List<String> {
        return options.filter { it.startsWith(input, ignoreCase = true) }
    }

    /** A hint like `<name>` while the argument is still empty. */
    protected fun hint(input: String, hint: String): List<String> {
        return if (input.isEmpty()) listOf(hint) else emptyList()
    }

}
