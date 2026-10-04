package gg.bitesize.rename.config

import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import java.util.EnumMap
import java.util.EnumSet
import java.util.Locale
import java.util.logging.Logger

enum class Feature(val key: String) {
    RENAME("rename"),
    LORE("lore"),
    CLEAR("clear"),
    HIDE("hide"),
    CLIPBOARD("clipboard"),
    TEMPLATES("templates"),
}

enum class CostAction(val key: String) {
    RENAME("rename"),
    LORE_ADD("lore-add"),
    LORE_DELLINE("lore-delline"),
    CLEAR_ALL("clear-all"),
    CLEAR_NAME("clear-name"),
    CLEAR_LORE("clear-lore"),
    HIDE("hide"),
    PASTE("paste"),
    TEMPLATE_LOAD("template-load"),
}

enum class EconomyType { NONE, VAULT, XP }

/**
 * Immutable snapshot of config.yml, rebuilt on load and reload
 * so commands never touch YAML on the hot path.
 */
class Settings private constructor(
    private val features: Set<Feature>,
    val maxLoreLines: Int,
    val maxNameLength: Int,
    val maxLoreLength: Int,
    val maxTemplates: Int,
    val uprightText: Boolean,
    private val blacklistedWords: List<String>,
    private val blacklistedMaterials: Set<Material>,
    val economyType: EconomyType,
    private val costs: Map<CostAction, Double>,
) {

    fun isEnabled(feature: Feature): Boolean = feature in features

    fun cost(action: CostAction): Double = costs[action] ?: 0.0

    /** Expects plain text, so formatting can't split a blocked word. */
    fun isWordBlacklisted(plainText: String): Boolean {
        if (blacklistedWords.isEmpty()) return false

        val lower = plainText.lowercase(Locale.ROOT)
        return blacklistedWords.any { it in lower }
    }

    fun isMaterialBlacklisted(material: Material): Boolean = material in blacklistedMaterials

    companion object {

        fun load(config: ConfigurationSection, logger: Logger): Settings {
            val features = EnumSet.noneOf(Feature::class.java)
            Feature.entries.filterTo(features) { config.getBoolean("features.${it.key}", true) }

            val materials = EnumSet.noneOf(Material::class.java)
            config.getStringList("blacklists.materials").forEach { name ->
                val material = Material.matchMaterial(name)
                if (material == null) {
                    logger.warning("Unknown material '$name' in blacklists.materials. Skipping it.")
                } else {
                    materials.add(material)
                }
            }

            val economyName = config.getString("economy.type", "NONE")!!.uppercase(Locale.ROOT)
            val economyType = EconomyType.entries.firstOrNull { it.name == economyName } ?: run {
                logger.warning("Unknown economy type '$economyName'. Use NONE, VAULT, or XP. Defaulting to NONE.")
                EconomyType.NONE
            }

            val costs = EnumMap<CostAction, Double>(CostAction::class.java)
            CostAction.entries.forEach { action ->
                costs[action] = config.getDouble("economy.costs.${action.key}", 0.0).coerceAtLeast(0.0)
            }

            return Settings(
                features = features,
                maxLoreLines = config.getInt("limits.max-lore-lines", 10).coerceAtLeast(0),
                maxNameLength = config.getInt("limits.max-name-length", 0).coerceAtLeast(0),
                maxLoreLength = config.getInt("limits.max-lore-length", 0).coerceAtLeast(0),
                maxTemplates = config.getInt("limits.max-templates", 0).coerceAtLeast(0),
                uprightText = config.getBoolean("formatting.upright-text", true),
                blacklistedWords = config.getStringList("blacklists.words")
                    .map { it.trim().lowercase(Locale.ROOT) }
                    .filter { it.isNotEmpty() },
                blacklistedMaterials = materials,
                economyType = economyType,
                costs = costs,
            )
        }

    }

}
