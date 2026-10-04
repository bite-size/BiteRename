package gg.bitesize.rename.commands.sub

import gg.bitesize.rename.commands.ItemSubCommand
import gg.bitesize.rename.commands.Permissions
import gg.bitesize.rename.config.CostAction
import gg.bitesize.rename.config.Feature
import gg.bitesize.rename.items.setFlag
import gg.bitesize.rename.managers.EconomyManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/** `/br hide <target>` when [hide] is true, `/br unhide <target>` otherwise. */
class FlagSubCommand(private val hide: Boolean) : ItemSubCommand(
    if (hide) "hide" else "unhide", Permissions.HIDE, "<enchants|attributes>",
    // hide and unhide share one help line
    helpKeys = if (hide) listOf("hide") else emptyList(),
    feature = Feature.HIDE,
) {

    private val flags = mapOf(
        "enchants" to ItemFlag.HIDE_ENCHANTS,
        "attributes" to ItemFlag.HIDE_ATTRIBUTES,
    )

    override fun execute(player: Player, item: ItemStack, label: String, args: List<String>) {
        val target = args.firstOrNull()?.lowercase()
        val flag = flags[target] ?: return sendUsage(player, label)

        if (item.itemMeta?.hasItemFlag(flag) == hide) {
            error(player, if (hide) "already-hidden" else "already-shown")
            return
        }

        if (!EconomyManager.charge(player, CostAction.HIDE)) return

        item.setFlag(flag, hide)
        success(player, "$target-${if (hide) "hidden" else "shown"}")
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return if (args.size == 1) matching(args[0], flags.keys) else emptyList()
    }

}
