package gg.bitesize.rename.commands.sub

import gg.bitesize.rename.commands.ItemSubCommand
import gg.bitesize.rename.commands.Permissions
import gg.bitesize.rename.config.CostAction
import gg.bitesize.rename.config.Feature
import gg.bitesize.rename.items.applySnapshot
import gg.bitesize.rename.managers.ClipboardManager
import gg.bitesize.rename.managers.EconomyManager
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CopySubCommand : ItemSubCommand("copy", Permissions.COPYPASTE, feature = Feature.CLIPBOARD) {

    override fun execute(player: Player, item: ItemStack, label: String, args: List<String>) {
        if (ClipboardManager.copy(player.uniqueId, item)) {
            success(player, "copied")
        } else {
            error(player, "nothing-to-copy")
        }
    }

}

class PasteSubCommand : ItemSubCommand("paste", Permissions.COPYPASTE, feature = Feature.CLIPBOARD) {

    override fun execute(player: Player, item: ItemStack, label: String, args: List<String>) {
        val clipboard = ClipboardManager.get(player.uniqueId) ?: return error(player, "clipboard-empty")

        if (!passesWordBlacklist(player, clipboard)) return
        if (!EconomyManager.charge(player, CostAction.PASTE)) return

        item.applySnapshot(clipboard, settings.uprightText)
        success(player, "pasted")
    }

}
