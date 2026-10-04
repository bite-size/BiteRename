package gg.bitesize.rename.commands

import gg.bitesize.rename.config.Feature
import gg.bitesize.rename.items.ItemSnapshot
import gg.bitesize.rename.items.ItemText
import gg.bitesize.rename.managers.FormatManager
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
        val item = player.inventory.itemInMainHand

        if (item.type.isAir) {
            error(player, "no-item")
            return
        }

        if (settings.isMaterialBlacklisted(item.type)) {
            if (!player.hasPermission(Permissions.BYPASS_BLACKLIST)) {
                error(player, "blacklisted-material")
                return
            }
            success(player, "bypassed-material-blacklist")
        }

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
        val component = ItemText.parse(raw)
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

    /** Checks copied or saved text too, so paste can't carry blocked words onto new items. */
    protected fun passesWordBlacklist(player: Player, snapshot: ItemSnapshot): Boolean {
        val text = listOfNotNull(snapshot.displayName) + snapshot.lore.orEmpty()
        val plain = text.joinToString("\n") { ItemText.plainText(FormatManager.fromLegacy(it)) }
        return passesWordBlacklist(player, plain)
    }

    private fun passesWordBlacklist(player: Player, plain: String): Boolean {
        if (!settings.isWordBlacklisted(plain)) return true

        if (!player.hasPermission(Permissions.BYPASS_BLACKLIST)) {
            error(player, "blacklisted-word")
            return false
        }

        success(player, "bypassed-word-blacklist")
        return true
    }

}
