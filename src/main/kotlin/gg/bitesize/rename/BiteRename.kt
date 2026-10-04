package gg.bitesize.rename

import gg.bitesize.rename.listeners.PlayerListener
import gg.bitesize.rename.managers.ClipboardManager
import gg.bitesize.rename.managers.CommandManager
import gg.bitesize.rename.managers.ConfigManager
import gg.bitesize.rename.managers.CooldownManager
import gg.bitesize.rename.managers.EconomyManager
import gg.bitesize.rename.managers.TemplateManager
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

class BiteRename : JavaPlugin() {

    companion object {
        lateinit var INSTANCE: BiteRename

        private const val BSTATS_PLUGIN_ID = 34507
    }

    private var metrics: Metrics? = null

    override fun onLoad() {
        INSTANCE = this
    }

    override fun onEnable() {
        ConfigManager.loadConfigs()
        TemplateManager.load()

        CommandManager.registerAll()
        CommandManager.syncCommands()

        server.pluginManager.registerEvents(PlayerListener, this)

        // Runs on the first tick, after every plugin (and its Vault economy) has enabled
        server.scheduler.runTask(this, Runnable { EconomyManager.checkSetup() })

        metrics = Metrics(this, BSTATS_PLUGIN_ID)
    }

    override fun onDisable() {
        metrics?.shutdown()

        CommandManager.shutdown()

        ClipboardManager.shutdown()

        TemplateManager.shutdown()

        CooldownManager.shutdown()
    }

}
