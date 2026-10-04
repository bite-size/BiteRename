package gg.bitesize.rename

import gg.bitesize.rename.managers.CommandManager
import gg.bitesize.rename.managers.ConfigManager
import gg.bitesize.rename.managers.CooldownManager
import org.bukkit.plugin.java.JavaPlugin

class BiteRename : JavaPlugin() {

    companion object {
        lateinit var INSTANCE: BiteRename
    }

    override fun onLoad() {
        INSTANCE = this
    }

    override fun onEnable() {
        ConfigManager.loadConfigs()

        CommandManager.registerAll()
        CommandManager.removeVanillaCommands()
        CommandManager.syncCommands()
    }

    override fun onDisable() {
        CommandManager.shutdown()

        CooldownManager.shutdown()
    }

}
