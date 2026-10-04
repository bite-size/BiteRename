package gg.bitesize.rename.managers

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit

/** Only touch [economy] after checking [isInstalled], so Vault classes never load without Vault. */
object VaultManager {

    val isInstalled: Boolean
        get() = Bukkit.getPluginManager().isPluginEnabled("Vault")

    // Looked up on use rather than at enable, since economy plugins may register after BiteRename
    val economy: Economy?
        get() = Bukkit.getServicesManager().getRegistration(Economy::class.java)?.provider

}
