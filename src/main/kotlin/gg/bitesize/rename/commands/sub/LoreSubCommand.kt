package gg.bitesize.rename.commands.sub

import gg.bitesize.rename.commands.ItemSubCommand
import gg.bitesize.rename.commands.Permissions
import gg.bitesize.rename.config.CostAction
import gg.bitesize.rename.config.Feature
import gg.bitesize.rename.items.ItemText
import gg.bitesize.rename.items.addLore
import gg.bitesize.rename.items.loreSize
import gg.bitesize.rename.items.removeLoreLine
import gg.bitesize.rename.managers.EconomyManager
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class LoreSubCommand : ItemSubCommand(
    "lore", Permissions.LORE, "<add|delline>",
    helpKeys = listOf("lore-add", "lore-delline"),
    feature = Feature.LORE,
) {

    private val actions = listOf("add", "delline")

    override fun execute(player: Player, item: ItemStack, label: String, args: List<String>) {
        when (args.firstOrNull()?.lowercase()) {
            "add" -> add(player, item, label, args.drop(1))
            "delline" -> deleteLine(player, item, label, args.drop(1))
            else -> sendUsage(player, label)
        }
    }

    private fun add(player: Player, item: ItemStack, label: String, args: List<String>) {
        if (args.isEmpty()) return sendUsage(player, label, "add <text>")

        val maxLines = settings.maxLoreLines
        if (maxLines > 0 && item.loreSize() >= maxLines) {
            error(player, "max-lore-lines", Placeholder.unparsed("max", maxLines.toString()))
            return
        }

        val line = parseText(player, args.joinToString(" "), settings.maxLoreLength, "lore-too-long") ?: return
        if (!EconomyManager.charge(player, CostAction.LORE_ADD)) return

        item.addLore(ItemText.toItemString(line, settings.uprightText), settings.uprightText)
        success(player, "lore-added")
    }

    private fun deleteLine(player: Player, item: ItemStack, label: String, args: List<String>) {
        val line = args.firstOrNull()?.toIntOrNull() ?: return sendUsage(player, label, "delline <line>")

        if (line !in 1..item.loreSize()) {
            error(player, "invalid-line")
            return
        }

        if (!EconomyManager.charge(player, CostAction.LORE_DELLINE)) return

        item.removeLoreLine(line, settings.uprightText)
        success(player, "lore-line-deleted", Placeholder.unparsed("line", line.toString()))
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when {
            args.size == 1 -> matching(args[0], actions)
            args.size == 2 && args[0].equals("add", ignoreCase = true) -> hint(args[1], "<text>")
            args.size == 2 && args[0].equals("delline", ignoreCase = true) -> {
                val lines = heldItem(sender)?.loreSize() ?: 0
                matching(args[1], (1..lines).map(Int::toString))
            }
            else -> emptyList()
        }
    }

}
