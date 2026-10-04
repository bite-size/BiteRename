package gg.bitesize.rename

import gg.bitesize.rename.listeners.PlayerListener
import gg.bitesize.rename.managers.ClipboardManager
import gg.bitesize.rename.managers.CommandManager
import gg.bitesize.rename.managers.ConfigManager
import gg.bitesize.rename.managers.CooldownManager
import gg.bitesize.rename.managers.TemplateManager
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
        TemplateManager.load()

        CommandManager.registerAll()
        CommandManager.syncCommands()

        server.pluginManager.registerEvents(PlayerListener, this)

        //bstats
    }

    override fun onDisable() {
        CommandManager.shutdown()

        ClipboardManager.shutdown()

        TemplateManager.shutdown()

        CooldownManager.shutdown()
    }

}
