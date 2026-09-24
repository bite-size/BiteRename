package gg.bitesize.template

import gg.bitesize.template.managers.ConfigManager
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    companion object {
        lateinit var INSTANCE: Main
    }

    override fun onLoad() {
        INSTANCE = this
    }

    override fun onEnable() {
        ConfigManager.loadConfigs()
    }

    override fun onDisable() {
        ConfigManager.saveConfigs()
    }

}