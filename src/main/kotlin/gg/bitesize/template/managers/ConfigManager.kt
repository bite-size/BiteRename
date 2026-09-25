package gg.bitesize.template.managers

import gg.bitesize.template.Main.Companion.INSTANCE
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object ConfigManager {

    private val CONFIG = INSTANCE.config

    private lateinit var messagesFile: File
    lateinit var messages: FileConfiguration
        private set

    private const val MESSAGE_NOT_FOUND = "<red>Message not found. Check <gray>messages.yml<red>.</red>"

    fun loadConfigs() {
        INSTANCE.saveDefaultConfig()

        messagesFile = File(INSTANCE.dataFolder, "messages.yml")
        if (!messagesFile.exists()) {
            INSTANCE.saveResource("messages.yml", false)
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile)
    }

    fun reloadConfigs() {
        INSTANCE.reloadConfig()

        messages = YamlConfiguration.loadConfiguration(messagesFile)
    }

    fun saveConfigs() {
        INSTANCE.saveConfig()
        try {
            messages.save(messagesFile)
        } catch (e: Exception) {
            INSTANCE.logger.severe("Could not save messages.yml to disk.")
            e.printStackTrace()
        }
    }

    fun getString(path: String, default: String = ""): String {
        return CONFIG.getString(path) ?: default
    }

    fun getInt(path: String, default: Int = 0): Int {
        return CONFIG.getInt(path, default)
    }

    fun getBoolean(path: String, default: Boolean = false): Boolean {
        return CONFIG.getBoolean(path, default)
    }

    fun getDouble(path: String, default: Double = 0.0): Double {
        return CONFIG.getDouble(path, default)
    }

    fun getList(path: String): List<String> {
        return CONFIG.getStringList(path)
    }

    fun getPrefix(): String {
        return messages.getString("prefix") ?: ""
    }

    fun getErrorPrefix(): String {
        return messages.getString("error-prefix") ?: "! "
    }

    fun getMessage(path: String, default: String = MESSAGE_NOT_FOUND): String {
        return messages.getString(path) ?: default
    }

}