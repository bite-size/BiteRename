package gg.bitesize.rename.commands.sub

import gg.bitesize.rename.commands.ItemSubCommand
import gg.bitesize.rename.commands.Permissions
import gg.bitesize.rename.config.CostAction
import gg.bitesize.rename.config.Feature
import gg.bitesize.rename.items.ItemText
import gg.bitesize.rename.items.rename
import gg.bitesize.rename.managers.EconomyManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class RenameSubCommand : ItemSubCommand("rename", Permissions.RENAME, "<name>", feature = Feature.RENAME) {

    override fun execute(player: Player, item: ItemStack, label: String, args: List<String>) {
        if (args.isEmpty()) return sendUsage(player, label)

        val name = parseText(player, args.joinToString(" "), settings.maxNameLength, "name-too-long") ?: return
        if (!EconomyManager.charge(player, CostAction.RENAME)) return

        item.rename(ItemText.toItemString(name, settings.uprightText))
        success(player, "item-renamed")
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return if (args.size == 1) hint(args[0], "<name>") else emptyList()
    }

}
