package gg.bitesize.rename

import gg.bitesize.rename.listeners.PlayerListener
import gg.bitesize.rename.managers.ClipboardManager
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
        CommandManager.syncCommands()

        server.pluginManager.registerEvents(PlayerListener, this)

        //bstats
    }

    override fun onDisable() {
        CommandManager.shutdown()

        ClipboardManager.shutdown()

        CooldownManager.shutdown()
    }

}
