package gg.bitesize.rename.managers

import gg.bitesize.rename.items.ItemSnapshot
import gg.bitesize.rename.items.snapshot
import org.bukkit.inventory.ItemStack
import java.util.UUID

object ClipboardManager {

    // Session-only: cleared when the player leaves. Templates are the persistent option.
    private val clipboards = HashMap<UUID, ItemSnapshot>()

    /** Copies the item's name, lore, and flags. Returns false if there's nothing to copy. */
    fun copy(playerId: UUID, item: ItemStack): Boolean {
        val snapshot = item.snapshot()?.takeUnless { it.isEmpty } ?: return false
        clipboards[playerId] = snapshot
        return true
    }

    fun get(playerId: UUID): ItemSnapshot? {
        return clipboards[playerId]
    }

    fun clear(playerId: UUID) {
        clipboards.remove(playerId)
    }

    fun shutdown() {
        clipboards.clear()
    }

}
