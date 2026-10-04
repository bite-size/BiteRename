package gg.bitesize.rename.managers

import gg.bitesize.rename.BiteRename.Companion.INSTANCE
import gg.bitesize.rename.items.ItemSnapshot
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemFlag
import org.bukkit.scheduler.BukkitTask
import java.io.File
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Locale
import java.util.UUID

/**
 * Per-player templates, kept in memory and saved to templates.yml.
 * Layout matches LRename: `<uuid>.<template>.{name, lore, flags}`.
 */
object TemplateManager {

    private val NAME_PATTERN = Regex("[a-z0-9_-]{1,32}")
    private const val SAVE_DELAY_TICKS = 40L

    private val templates = HashMap<UUID, LinkedHashMap<String, ItemSnapshot>>()

    private lateinit var file: File
    private var pendingSave: BukkitTask? = null
    private var saveSequence = 0L
    private var lastWrittenSequence = 0L

    fun load() {
        file = File(INSTANCE.dataFolder, "templates.yml")
        templates.clear()

        if (!file.exists()) {
            importFromLRename()
            return
        }

        val count = read(YamlConfiguration.loadConfiguration(file))
        INSTANCE.logger.info("Loaded $count templates.")
    }

    /** Lowercases a template name, or returns null if it isn't a valid name. */
    fun normalize(name: String): String? {
        return name.lowercase(Locale.ROOT).takeIf { NAME_PATTERN.matches(it) }
    }

    fun names(playerId: UUID): List<String> {
        return templates[playerId]?.keys?.toList() ?: emptyList()
    }

    fun count(playerId: UUID): Int {
        return templates[playerId]?.size ?: 0
    }

    fun get(playerId: UUID, name: String): ItemSnapshot? {
        return templates[playerId]?.get(name)
    }

    fun save(playerId: UUID, name: String, snapshot: ItemSnapshot) {
        templates.getOrPut(playerId) { LinkedHashMap() }[name] = snapshot
        scheduleSave()
    }

    fun delete(playerId: UUID, name: String): Boolean {
        val playerTemplates = templates[playerId] ?: return false
        playerTemplates.remove(name) ?: return false

        if (playerTemplates.isEmpty()) templates.remove(playerId)
        scheduleSave()
        return true
    }

    fun shutdown() {
        pendingSave?.let {
            it.cancel()
            pendingSave = null
            write(serialize(), ++saveSequence)
        }
    }

    // Batches changes made within a couple of seconds into one write.
    // The YAML is built on the main thread and written off it.
    private fun scheduleSave() {
        if (pendingSave != null) return

        pendingSave = INSTANCE.server.scheduler.runTaskLater(INSTANCE, Runnable {
            pendingSave = null
            val content = serialize()
            val sequence = ++saveSequence
            INSTANCE.server.scheduler.runTaskAsynchronously(INSTANCE, Runnable { write(content, sequence) })
        }, SAVE_DELAY_TICKS)
    }

    private fun serialize(): String {
        val yaml = YamlConfiguration()
        yaml.options().setHeader(listOf(
            "BiteRename templates, keyed by player UUID.",
            "Edit only while the server is stopped; changes are overwritten otherwise.",
        ))

        templates.forEach { (playerId, playerTemplates) ->
            playerTemplates.forEach { (name, snapshot) ->
                val path = "$playerId.$name"
                snapshot.displayName?.let { yaml.set("$path.name", it) }
                snapshot.lore?.let { yaml.set("$path.lore", it) }
                yaml.set("$path.flags", snapshot.flags.map { it.name })
            }
        }

        return yaml.saveToString()
    }

    // Synchronized with a sequence check, so an older async write can't land after a newer one
    @Synchronized
    private fun write(content: String, sequence: Long) {
        if (sequence < lastWrittenSequence) return

        try {
            file.parentFile.mkdirs()
            val temp = File(file.parentFile, "${file.name}.tmp").toPath()
            Files.writeString(temp, content)

            try {
                Files.move(temp, file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            } catch (e: AtomicMoveNotSupportedException) {
                Files.move(temp, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }

            lastWrittenSequence = sequence
        } catch (e: IOException) {
            INSTANCE.logger.severe("Could not save templates.yml: ${e.message}")
        }
    }

    /** Reads every valid template from [yaml] into memory and returns how many were loaded. */
    private fun read(yaml: ConfigurationSection): Int {
        var count = 0

        yaml.getKeys(false).forEach { key ->
            val playerId = runCatching { UUID.fromString(key) }.getOrNull()
            val section = yaml.getConfigurationSection(key)
            if (playerId == null || section == null) {
                INSTANCE.logger.warning("Skipping invalid player entry '$key' in templates.")
                return@forEach
            }

            section.getKeys(false).forEach templates@{ rawName ->
                val name = normalize(rawName)
                val template = section.getConfigurationSection(rawName)?.let(::readTemplate)
                if (name == null || template == null) {
                    INSTANCE.logger.warning("Skipping invalid template '$rawName' for $key.")
                    return@templates
                }

                templates.getOrPut(playerId) { LinkedHashMap() }[name] = template
                count++
            }
        }

        return count
    }

    private fun readTemplate(section: ConfigurationSection): ItemSnapshot? {
        // Type checks matter: getString() would turn a nested section into its toString()
        val flags = section.getStringList("flags").mapNotNull { flagName ->
            ItemFlag.entries.firstOrNull { it.name.equals(flagName, ignoreCase = true) }
        }.toSet()

        return ItemSnapshot(
            displayName = if (section.isString("name")) section.getString("name") else null,
            lore = if (section.isList("lore")) section.getStringList("lore") else null,
            flags = flags,
        ).takeUnless { it.isEmpty }
    }

    // One-time import on first start, from LRename's templates.yml (same layout)
    private fun importFromLRename() {
        val legacyFile = File(INSTANCE.dataFolder.parentFile, "LRename/templates.yml")
        if (!legacyFile.exists()) return

        val count = read(YamlConfiguration.loadConfiguration(legacyFile))
        if (count == 0) return

        write(serialize(), ++saveSequence)
        INSTANCE.logger.info("Imported $count templates from LRename.")
    }

}
