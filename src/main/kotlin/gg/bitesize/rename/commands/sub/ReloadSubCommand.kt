package gg.bitesize.rename.commands.sub

import gg.bitesize.rename.commands.Permissions
import gg.bitesize.rename.commands.SubCommand
import gg.bitesize.rename.managers.ConfigManager
import gg.bitesize.rename.managers.EconomyManager
import org.bukkit.command.CommandSender

class ReloadSubCommand : SubCommand("reload", Permissions.ADMIN, playerOnly = false) {

    override fun execute(sender: CommandSender, label: String, args: List<String>) {
        ConfigManager.reloadConfigs()
        EconomyManager.checkSetup()
        success(sender, "plugin-reloaded")
    }

}
