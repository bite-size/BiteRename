package gg.bitesize.bitertp

import io.papermc.paper.command.brigadier.argument.ArgumentTypes.world
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Display
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.jvm.java

class NPCManager(plugin: BiteRTP) {

    val npcIdKey = NamespacedKey(plugin, "rtp")

    fun spawnRTPCookie() {
        val world = Bukkit.getWorld("spawn") ?: return
        val loc = Location(world, 0.5, 100.0, 32.5, 180F, 0F)

        deleteRTPCookie()

        world.spawn(loc, ItemDisplay::class.java) { display ->
            display.setItemStack(ItemStack(Material.COOKIE))
            display.transformation = Transformation(
                Vector3f(0f, 0f, 0f),
                Quaternionf(),
                Vector3f(8f, 8f, 8f),
                Quaternionf()
            )
            display.itemDisplayTransform = ItemDisplay.ItemDisplayTransform.GROUND
            display.isInvulnerable = true
            display.setGravity(false)
            display.persistentDataContainer.set(npcIdKey, PersistentDataType.STRING, "rtp_visual")
        }

        world.spawn(loc, Interaction::class.java) { interaction ->
            interaction.interactionWidth = 3.5f
            interaction.interactionHeight = 4.0f
            interaction.isInvulnerable = true
            interaction.setGravity(false)
            interaction.persistentDataContainer.set(npcIdKey, PersistentDataType.STRING, "rtp")
        }

        world.spawn(loc.add(0.0, 2.85, 0.0), TextDisplay::class.java) { display ->
            display.text(Component.text("ʀᴀɴᴅᴏᴍ ᴛᴇʟᴇᴘᴏʀᴛ").color(NamedTextColor.RED).decorate(TextDecoration.BOLD))
            display.isDefaultBackground = true
            display.isInvulnerable = true
            display.setGravity(false)
            display.billboard = Display.Billboard.FIXED
            display.alignment = TextDisplay.TextAlignment.CENTER
            display.persistentDataContainer.set(npcIdKey, PersistentDataType.STRING, "rtp_text")
        }
    }

    fun deleteRTPCookie() {
        val world = Bukkit.getWorld("spawn") ?: return
        val loc = Location(world, 0.5, 100.0, 32.5)

        if (!loc.chunk.isLoaded) {
            loc.chunk.load()
        }

        loc.getNearbyEntities(5.0, 5.0, 5.0).forEach { entity ->
            val pdc = entity.persistentDataContainer

            when (entity) {
                is ItemDisplay -> {
                    if (pdc.get(npcIdKey, PersistentDataType.STRING) == "rtp_visual") {
                        entity.remove()
                    }
                }
                is Interaction -> {
                    if (pdc.get(npcIdKey, PersistentDataType.STRING) == "rtp") {
                        entity.remove()
                    }
                }
                is TextDisplay -> {
                    if (pdc.get(npcIdKey, PersistentDataType.STRING) == "rtp_text") {
                        entity.remove()
                    }
                }
            }
        }
    }
}