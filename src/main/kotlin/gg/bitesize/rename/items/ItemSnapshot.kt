package gg.bitesize.rename.items

import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta

/**
 * The editable presentation of an item: name, lore, and flags.
 * Text is stored as section-sign strings, exactly as the item holds it.
 */
data class ItemSnapshot(
    val displayName: String?,
    val lore: List<String>?,
    val flags: Set<ItemFlag>,
) {

    val isEmpty: Boolean
        get() = displayName.isNullOrEmpty() && lore.isNullOrEmpty() && flags.isEmpty()

    /** Replaces the meta's name, lore, and flags, so the result matches the snapshot exactly. */
    fun applyTo(meta: ItemMeta) {
        meta.setDisplayName(displayName?.takeIf { it.isNotEmpty() })
        meta.lore = lore?.takeIf { it.isNotEmpty() }

        meta.removeItemFlags(*ItemFlag.entries.toTypedArray())
        if (flags.isNotEmpty()) {
            meta.addItemFlags(*flags.toTypedArray())
        }
    }

    companion object {

        fun from(meta: ItemMeta): ItemSnapshot {
            return ItemSnapshot(
                displayName = if (meta.hasDisplayName()) meta.displayName else null,
                lore = if (meta.hasLore()) meta.lore?.toList() else null,
                flags = meta.itemFlags.toSet(),
            )
        }

    }

}
