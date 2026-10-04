package gg.bitesize.rename.managers

import gg.bitesize.rename.BiteRename.Companion.INSTANCE
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
            EconomyType.VAULT -> chargeMoney(player, cost)
        }
    }

    /** Logs a warning if Vault is selected but unusable. Run once plugins have finished enabling. */
    fun checkSetup() {
        if (ConfigManager.settings.economyType != EconomyType.VAULT) return

        when {
            !VaultManager.isInstalled ->
                INSTANCE.logger.warning("Economy type is VAULT, but Vault isn't installed. Paid actions will be refused.")
            VaultManager.economy == null ->
                INSTANCE.logger.warning("Economy type is VAULT, but no economy plugin is registered with Vault. Paid actions will be refused.")
            else ->
                INSTANCE.logger.info("Hooked into ${VaultManager.economy?.name} through Vault.")
        }
    }

    private fun chargeMoney(player: Player, cost: Double): Boolean {
        val economy = if (VaultManager.isInstalled) VaultManager.economy else null
        if (economy == null) {
            FormatManager.error(player, ConfigManager.getMessage("errors.vault-unavailable"))
            return false
        }

        if (!economy.has(player, cost)) {
            FormatManager.error(player, ConfigManager.getMessage("errors.insufficient-funds"),
                Placeholder.unparsed("cost", economy.format(cost)))
            return false
        }

        if (!economy.withdrawPlayer(player, cost).transactionSuccess()) {
            FormatManager.error(player, ConfigManager.getMessage("errors.transaction-failed"))
            return false
        }

        return true
    }

    private fun chargeLevels(player: Player, levels: Int): Boolean {
        if (levels <= 0) return true

        if (player.level < levels) {
            FormatManager.error(player, ConfigManager.getMessage("errors.insufficient-xp"),
                Placeholder.unparsed("cost", levels.toString()))
            return false
        }

        player.level -= levels
        return true
    }

}
