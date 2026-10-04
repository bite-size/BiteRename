package gg.bitesize.rename.managers

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PapiManager {

    val isInstalled: Boolean
        get() = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")

    fun parse(player: Player, text: String): String {
        return if (isInstalled) PlaceholderAPI.setPlaceholders(player, text) else text
    }

}
