package gg.bitesize.rename.managers

import gg.bitesize.rename.commands.Permissions
import gg.bitesize.rename.config.CostAction
import gg.bitesize.rename.config.EconomyType
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player

object EconomyManager {

    /**
     * Charges the player for an action. Call it only after every other check has passed.
     * Returns false after messaging the player if they can't pay.
     */
    fun charge(player: Player, action: CostAction): Boolean {
        val settings = ConfigManager.settings
        val cost = settings.cost(action)

        if (cost <= 0.0 || player.hasPermission(Permissions.BYPASS_COST)) return true

        return when (settings.economyType) {
            EconomyType.NONE -> true
            EconomyType.XP -> chargeLevels(player, cost.toInt())
            EconomyType.VAULT -> {
                FormatManager.error(player, ConfigManager.getMessage("economy-unavailable"))
                false
            }
        }
    }

    private fun chargeLevels(player: Player, levels: Int): Boolean {
        if (levels <= 0) return true

        if (player.level < levels) {
            FormatManager.error(player, ConfigManager.getMessage("not-enough-xp"),
                Placeholder.unparsed("cost", levels.toString()))
            return false
        }

        player.level -= levels
        return true
    }

}
