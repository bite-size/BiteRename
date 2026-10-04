package gg.bitesize.rename.commands.sub

import gg.bitesize.rename.commands.ItemSubCommand
import gg.bitesize.rename.commands.Permissions
import gg.bitesize.rename.config.CostAction
import gg.bitesize.rename.config.Feature
import gg.bitesize.rename.items.clearAll
import gg.bitesize.rename.items.clearLore
import gg.bitesize.rename.items.clearName
import gg.bitesize.rename.managers.EconomyManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ClearSubCommand : ItemSubCommand("clear", Permissions.CLEAR, "[name|lore]", feature = Feature.CLEAR) {

    private val targets = listOf("name", "lore")

    override fun execute(player: Player, item: ItemStack, label: String, args: List<String>) {
        val meta = item.itemMeta ?: return
        val hasName = meta.hasDisplayName()
        val hasLore = meta.hasLore()

        when (args.firstOrNull()?.lowercase()) {
            null -> {
                if (!hasName && !hasLore) return error(player, "nothing-to-clear")
                if (!EconomyManager.charge(player, CostAction.CLEAR_ALL)) return
                item.clearAll()
                success(player, "all-cleared")
            }
            "name" -> {
                if (!hasName) return error(player, "nothing-to-clear")
                if (!EconomyManager.charge(player, CostAction.CLEAR_NAME)) return
                item.clearName()
                success(player, "name-cleared")
            }
            "lore" -> {
                if (!hasLore) return error(player, "nothing-to-clear")
                if (!EconomyManager.charge(player, CostAction.CLEAR_LORE)) return
                item.clearLore()
                success(player, "lore-cleared")
            }
            else -> sendUsage(player, label)
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return if (args.size == 1) matching(args[0], targets) else emptyList()
    }

}
