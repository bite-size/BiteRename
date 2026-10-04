package gg.bitesize.rename.commands.sub

import gg.bitesize.rename.commands.Permissions
import gg.bitesize.rename.commands.SubCommand
import gg.bitesize.rename.managers.ConfigManager
import org.bukkit.command.CommandSender

class ReloadSubCommand : SubCommand("reload", Permissions.ADMIN, playerOnly = false) {

    override fun execute(sender: CommandSender, label: String, args: List<String>) {
        ConfigManager.reloadConfigs()
        success(sender, "reloaded")
    }

}
