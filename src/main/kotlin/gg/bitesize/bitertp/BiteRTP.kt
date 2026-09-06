package gg.bitesize.bitertp

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class BiteRTP : JavaPlugin() {

    private val npcManager = NPCManager(this)

    override fun onEnable() {
        val rtpService = RTPService(this)
        val rtpMenu = RTPMenu(rtpService)
        Bukkit.getPluginManager().registerEvents(NPCListener(npcManager, rtpMenu), this)
        Bukkit.getPluginManager().registerEvents(rtpMenu, this)
        Bukkit.getPluginManager().registerEvents(rtpService, this)
        npcManager.spawnRTPCookie()
    }

    override fun onDisable() {
        npcManager.deleteRTPCookie()
    }

}