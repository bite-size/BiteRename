package gg.bitesize.rename.commands

import gg.bitesize.rename.config.Feature
import gg.bitesize.rename.items.ItemText
import gg.bitesize.rename.managers.PapiManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * A subcommand that edits the item in the player's main hand.
 * Flow: held item -> material blacklist -> subcommand validation -> charge -> apply.
 */
abstract class ItemSubCommand(
    name: String,
    permission: String,
    helpKeys: List<String> = listOf(name),
    feature: Feature,
) : SubCommand(name, permission, helpKeys, feature, playerOnly = true) {

    final override fun execute(sender: CommandSender, label: String, args: List<String>) {
        val player = sender as Player
        val item = editableItem(player) ?: return
        execute(player, item, label, args)
    }

    abstract fun execute(player: Player, item: ItemStack, label: String, args: List<String>)

    protected fun heldItem(sender: CommandSender): ItemStack? {
        return (sender as? Player)?.inventory?.itemInMainHand?.takeUnless { it.type.isAir }
    }

    /**
     * Parses player-written text and applies the length limit and word blacklist.
     * Returns null after messaging the player if the text is rejected.
     */
    protected fun parseText(player: Player, raw: String, maxLength: Int, tooLongKey: String): Component? {
        // Gated by permission, since placeholders can expose server or other players' data
        val text = if (player.hasPermission(Permissions.PLACEHOLDERS)) PapiManager.parse(player, raw) else raw

        val component = ItemText.parse(text)
        val plain = ItemText.plainText(component)

        if (plain.isBlank()) {
            error(player, "empty-text")
            return null
        }

        if (maxLength > 0 && plain.codePointCount(0, plain.length) > maxLength) {
            error(player, tooLongKey, Placeholder.unparsed("max", maxLength.toString()))
            return null
        }

        return component.takeIf { passesWordBlacklist(player, plain) }
    }

}
