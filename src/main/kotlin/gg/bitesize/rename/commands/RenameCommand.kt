package gg.bitesize.rename.commands

import gg.bitesize.rename.managers.ConfigManager
import gg.bitesize.rename.managers.FormatManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class RenameCommand : BaseCommand("biterename", aliases = listOf("br")) {

    override fun execute(sender: CommandSender, label: String, args: Array<String>): Boolean {
        if(sender !is Player) {
            FormatManager.error(sender, ConfigManager.getMessage("not-a-player"))
            return true
        }

        if(!sender.hasPermission("biterename.use")) {
            FormatManager.error(sender, ConfigManager.getMessage("no-permission"))
            return true
        }

        ConfigManager.getMessageList("help").forEach { line ->
            FormatManager.send(sender, line, usePrefix = false)
        }

        return true
    }

}
