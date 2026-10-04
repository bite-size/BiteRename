package gg.bitesize.rename.commands

import gg.bitesize.rename.commands.sub.ClearSubCommand
import gg.bitesize.rename.commands.sub.CopySubCommand
import gg.bitesize.rename.commands.sub.FlagSubCommand
import gg.bitesize.rename.commands.sub.LoreSubCommand
import gg.bitesize.rename.commands.sub.PasteSubCommand
import gg.bitesize.rename.commands.sub.ReloadSubCommand
import gg.bitesize.rename.commands.sub.RenameSubCommand
import gg.bitesize.rename.managers.ConfigManager
import gg.bitesize.rename.managers.FormatManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.Locale

class RenameCommand : BaseCommand("biterename", aliases = listOf("br")) {

    // Declaration order is the help and tab-completion order
    private val subCommands: Map<String, SubCommand> = listOf(
        RenameSubCommand(),
        LoreSubCommand(),
        ClearSubCommand(),
        FlagSubCommand(hide = true),
        FlagSubCommand(hide = false),
        CopySubCommand(),
        PasteSubCommand(),
        ReloadSubCommand(),
    ).associateBy { it.name }

    init {
        // Hides the command from players who can't use it
        permission = Permissions.USE
    }

    override fun execute(sender: CommandSender, label: String, args: Array<String>): Boolean {
        if(!sender.hasPermission(Permissions.USE)) {
            FormatManager.error(sender, ConfigManager.getMessage("no-permission"))
            return true
        }

        val subCommand = args.firstOrNull()?.lowercase(Locale.ROOT)?.let(subCommands::get)
        if (subCommand == null) {
            sendHelp(sender)
            return true
        }

        when {
            subCommand.playerOnly && sender !is Player ->
                FormatManager.error(sender, ConfigManager.getMessage("not-a-player"))
            !sender.hasPermission(subCommand.permission) ->
                FormatManager.error(sender, ConfigManager.getMessage("no-permission"))
            subCommand.feature != null && !ConfigManager.settings.isEnabled(subCommand.feature) ->
                FormatManager.error(sender, ConfigManager.getMessage("feature-disabled"))
            else ->
                subCommand.execute(sender, label, args.drop(1))
        }

        return true
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>): List<String> {
        if (!sender.hasPermission(Permissions.USE)) return emptyList()

        if (args.size <= 1) {
            val input = args.firstOrNull() ?: ""
            return subCommands.values
                .filter { it.isAvailableTo(sender) && it.name.startsWith(input, ignoreCase = true) }
                .map { it.name }
        }

        val subCommand = subCommands[args[0].lowercase(Locale.ROOT)]
            ?.takeIf { it.isAvailableTo(sender) }
            ?: return emptyList()

        return subCommand.tabComplete(sender, args.drop(1))
    }

    private fun sendHelp(sender: CommandSender) {
        val entries = subCommands.values
            .filter { it.isAvailableTo(sender) }
            .flatMap { it.helpKeys }
            .map { ConfigManager.getMessage("help.entries.$it") }

        FormatManager.sendList(sender, listOf(ConfigManager.getMessage("help.header")) + entries)
    }

}
