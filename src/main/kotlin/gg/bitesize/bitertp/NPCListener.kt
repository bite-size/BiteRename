package gg.bitesize.bitertp

import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType

class NPCListener(private val npcManager: NPCManager, private val rtpMenu: RTPMenu) : Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractEntityEvent) {
        if(event.hand != EquipmentSlot.HAND) return
        if(event.rightClicked !is Interaction) return
        if(event.rightClicked.persistentDataContainer.get(npcManager.npcIdKey, PersistentDataType.STRING) != "rtp") return

        event.isCancelled = true
        rtpMenu.showRTPMenu(event.player)
    }
}