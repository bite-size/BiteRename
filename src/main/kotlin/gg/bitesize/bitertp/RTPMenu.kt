package gg.bitesize.bitertp

import io.papermc.paper.command.brigadier.argument.ArgumentTypes.world
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.event.player.PlayerCustomClickEvent
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RTPMenu(private val rtpService: RTPService) : Listener {

    val rtpDialog = Dialog.create { builder ->
        builder
            .empty()
            .base(
                DialogBase.builder(Component.text("Random Teleport"))
                    .body(listOf(
                        DialogBody.plainMessage(Component.text("Choose where to randomly teleport.").color(
                            NamedTextColor.YELLOW))
                    ))
                    .build()
                )
            .type(DialogType.multiAction(listOf(
                    ActionButton.builder(Component.text("Overworld").color(NamedTextColor.GREEN))
                        .tooltip(Component.text("Teleport Price: ").color(NamedTextColor.GRAY).append(Component.text("FREE").color(
                            NamedTextColor.GREEN)))
                        .action(DialogAction.customClick(Key.key("rtpmenu:overworld"), null))
                        .build(),
                    ActionButton.builder(Component.text("Nether").color(NamedTextColor.RED))
                        .tooltip(Component.text("Teleport Price: ").color(NamedTextColor.GRAY).append(Component.text("$500").color(
                            NamedTextColor.RED)))
                        .action(DialogAction.customClick(Key.key("rtpmenu:nether"), null))
                        .build(),
                    ActionButton.builder(Component.text("End").color(NamedTextColor.LIGHT_PURPLE))
                        .tooltip(Component.text("Teleport Price: ").color(NamedTextColor.GRAY).append(Component.text("$1000").color(
                            NamedTextColor.RED)))
                        .action(DialogAction.customClick(Key.key("rtpmenu:end"), null))
                        .build()
                )).build()
            )
    }

    fun showConfirmationDialog(player: Player, answer: String, teleportPrice: Double) {
        val confirmationDialog = Dialog.create { builder ->
            builder
                .empty()
                .base(
                    DialogBase.builder(Component.text(""))
                        .body(listOf(
                            DialogBody.plainMessage(Component.text("Confirm Teleport for $$teleportPrice?").color(NamedTextColor.RED))
                        ))
                        .build()
                )
                .type(
                    DialogType.confirmation(
                        ActionButton.builder(Component.text("Yes").color(NamedTextColor.GREEN))
                            .action(DialogAction.customClick(Key.key("confirmtpmenu:yes_$answer"), null))
                            .build(),
                        ActionButton.builder(Component.text("No").color(NamedTextColor.RED))
                            .action(DialogAction.customClick(Key.key("confirmtpmenu:no"), null))
                            .build()
                    )
                )
        }
        player.showDialog(confirmationDialog)
    }

    fun showRTPMenu(player: Player) {
        player.showDialog(rtpDialog)
        player.playSound(player.location, Sound.BLOCK_END_PORTAL_FRAME_FILL, 0.2f, 0.2f)
    }

    @EventHandler
    fun onPlayerCustomClick(event: PlayerCustomClickEvent) {
        val key = event.identifier.asString()
        val player = (event.commonConnection as PlayerGameConnection).player


        if(key.startsWith("rtpmenu:")) {
            val answer = key.substringAfter("rtpmenu:")
            var teleportPrice = 0.0
            when(answer) {
                "overworld" -> {
                    teleportPrice = 0.0
                }
                "nether" -> {
                    teleportPrice = 500.0
                }
                "end" -> {
                    teleportPrice = 1000.0
                }
            }
            showConfirmationDialog(player, answer, teleportPrice)
        }

        if(key.startsWith("confirmtpmenu:")) {
            val answer = key.substringAfter("confirmtpmenu:")
            var worldName = ""
            when(answer) {
                "yes_overworld" -> {
                    worldName = "world"
                }
                "yes_nether" -> {
                    worldName = "world_nether"
                }
                "yes_end" -> {
                    worldName = "world_the_end"
                }
            }
            val world = Bukkit.getWorld(worldName)
            if(world == null) {
                if(answer != "no") player.sendMessage(Component.text("Oops.. this shouldn't have happend! Error: World name is NULL.").color(NamedTextColor.RED))
                return
            }
            rtpService.onRTP(player, world)
        }
    }


}