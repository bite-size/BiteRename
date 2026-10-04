package gg.bitesize.rename.commands

import gg.bitesize.rename.managers.ConfigManager
import gg.bitesize.rename.managers.FormatManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class RenameCommand : BaseCommand("biterename", aliases = listOf("br")) {

    private val helpEntries = listOf(
        "rename", "lore-add", "lore-delline", "clear", "hide",
        "copy", "paste", "template", "templates", "reload",
    )

    override fun execute(sender: CommandSender, label: String, args: Array<String>): Boolean {
        if(sender !is Player) {
            FormatManager.error(sender, ConfigManager.getMessage("not-a-player"))
            return true
        }

        if(!sender.hasPermission("biterename.use")) {
            FormatManager.error(sender, ConfigManager.getMessage("no-permission"))
            return true
        }

        val lines = listOf(ConfigManager.getMessage("help.header")) +
                helpEntries.map { ConfigManager.getMessage("help.entries.$it") }
        FormatManager.sendList(sender, lines)

        return true
    }

}
