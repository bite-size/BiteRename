package gg.bitesize.rename.listeners

import gg.bitesize.rename.managers.ClipboardManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

object PlayerListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        ClipboardManager.clear(event.player.uniqueId)
    }

}
