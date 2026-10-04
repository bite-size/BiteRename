package gg.bitesize.rename.items

import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

// Edits go through getItemMeta/setItemMeta, which Spigot, Paper, and Purpur all share.
// Each returns false when the item can't hold meta (air).

/** Cross-platform stand-in for Paper's ItemStack#editMeta. */
inline fun ItemStack.edit(block: (ItemMeta) -> Unit): Boolean {
    val meta = itemMeta ?: return false
    block(meta)
    itemMeta = meta
    return true
}

fun ItemStack.rename(name: String): Boolean = edit { it.setDisplayName(name) }

fun ItemStack.loreSize(): Int {
    val meta = itemMeta ?: return 0
    return if (meta.hasLore()) meta.lore?.size ?: 0 else 0
}

fun ItemStack.addLore(line: String): Boolean = edit { meta ->
    meta.lore = (meta.lore ?: emptyList()) + line
}

/** Removes a 1-indexed lore line. */
fun ItemStack.removeLoreLine(line: Int): Boolean {
    val meta = itemMeta ?: return false
    val lore = meta.lore?.toMutableList() ?: return false
    if (line !in 1..lore.size) return false

    lore.removeAt(line - 1)
    meta.lore = lore.ifEmpty { null }
    itemMeta = meta
    return true
}

fun ItemStack.clearName(): Boolean = edit { it.setDisplayName(null) }

fun ItemStack.clearLore(): Boolean = edit { it.lore = null }

fun ItemStack.clearAll(): Boolean = edit { meta ->
    meta.setDisplayName(null)
    meta.lore = null
}

fun ItemStack.setFlag(flag: ItemFlag, enabled: Boolean): Boolean = edit { meta ->
    if (enabled) meta.addItemFlags(flag) else meta.removeItemFlags(flag)
}

fun ItemStack.snapshot(): ItemSnapshot? = itemMeta?.let(ItemSnapshot::from)

fun ItemStack.applySnapshot(snapshot: ItemSnapshot): Boolean = edit(snapshot::applyTo)
