package gg.bitesize.bitertp

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import java.util.Random
import kotlin.collections.remove

class RTPService(val plugin: BiteRTP) : Listener {

    private val MAX_X = 150 // 250
    private val MAX_Z = 150

    companion object {
        val dontMove = mutableListOf<Player>()
    }

    fun onRTP(player: Player, world: World) {
        var rtpLocation = findRandomLocation(world)
        if(!isSafeLocation(rtpLocation)) rtpLocation = findRandomLocation(world)
        player.sendActionBar(Component.text("Don't move for ").color(NamedTextColor.GRAY)
            .append(Component.text("3 seconds", NamedTextColor.RED))
            .append(Component.text(".", NamedTextColor.GRAY)))
        dontMove.add(player)
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if(!dontMove.contains(player)) return@Runnable

            player.sendActionBar(Component.text("Teleporting...").color(NamedTextColor.GRAY))
            player.teleport(rtpLocation.add(0.5, 1.0, 0.5))
            dontMove.remove(player)
        }, 60L)
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        val player = event.player

        if(!dontMove.contains(player)) return
        if(event.from.blockX == event.to.blockX && event.from.blockZ == event.to.blockZ) return

        dontMove.remove(player)
        player.sendActionBar(Component.text("Canceled!").color(NamedTextColor.RED))

    }

    fun findRandomLocation(world: World): Location {
        val random = Random()

        val randomX = random.nextInt(-(MAX_X), MAX_X).toDouble()
        val randomZ = random.nextInt(-(MAX_Z), MAX_Z).toDouble()
        var rtpLocation = Location(world, randomX, 0.0, randomZ)
        val randomY = world.getHighestBlockAt(rtpLocation).y.toDouble()

        return Location(world, randomX, randomY, randomZ)
    }

    fun isSafeLocation(location: Location): Boolean {
        val block = location.block
        return !(block.type == Material.LAVA || block.type == Material.AIR)
    }
}