package gg.bitesize.rename.commands.sub

import gg.bitesize.rename.commands.Permissions
import gg.bitesize.rename.commands.SubCommand
import gg.bitesize.rename.config.CostAction
import gg.bitesize.rename.config.Feature
import gg.bitesize.rename.items.applySnapshot
import gg.bitesize.rename.items.snapshot
import gg.bitesize.rename.managers.EconomyManager
import gg.bitesize.rename.managers.TemplateManager
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.Locale

class TemplateSubCommand : SubCommand(
    "template", Permissions.TEMPLATES,
    helpKeys = listOf("template-save", "template-load", "template-delete"),
    feature = Feature.TEMPLATES,
) {

    private val actions = listOf("save", "load", "delete")

    override fun execute(sender: CommandSender, label: String, args: List<String>) {
        val player = sender as Player
        val action = args.firstOrNull()?.lowercase(Locale.ROOT)
        if (action !in actions) return sendUsage(player, label)

        val rawName = args.getOrNull(1) ?: return sendUsage(player, label, "template-$action")

        when (action) {
            "save" -> save(player, rawName)
            "load" -> load(player, rawName)
            "delete" -> delete(player, rawName)
        }
    }

    private fun save(player: Player, rawName: String) {
        val name = TemplateManager.normalize(rawName) ?: return error(player, "invalid-template-name")
        val item = editableItem(player) ?: return

        val snapshot = item.snapshot()?.takeUnless { it.isEmpty } ?: return error(player, "nothing-to-save")

        // Overwriting an existing template doesn't count toward the limit
        val max = settings.maxTemplates
        val overwriting = TemplateManager.get(player.uniqueId, name) != null
        if (max > 0 && !overwriting && TemplateManager.count(player.uniqueId) >= max) {
            error(player, "template-limit", Placeholder.unparsed("max", max.toString()))
            return
        }

        TemplateManager.save(player.uniqueId, name, snapshot)
        success(player, "template-saved", templatePlaceholder(name))
    }

    private fun load(player: Player, rawName: String) {
        val name = rawName.lowercase(Locale.ROOT)
        val template = TemplateManager.get(player.uniqueId, name) ?: return error(player, "template-not-found")
        val item = editableItem(player) ?: return

        if (!passesWordBlacklist(player, template)) return
        if (!EconomyManager.charge(player, CostAction.TEMPLATE_LOAD)) return

        item.applySnapshot(template, settings.uprightText)
        success(player, "template-loaded", templatePlaceholder(name))
    }

    private fun delete(player: Player, rawName: String) {
        val name = rawName.lowercase(Locale.ROOT)
        if (!TemplateManager.delete(player.uniqueId, name)) return error(player, "template-not-found")

        success(player, "template-deleted", templatePlaceholder(name))
    }

    private fun templatePlaceholder(name: String) = Placeholder.unparsed("template", name)

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val player = sender as? Player ?: return emptyList()

        return when {
            args.size == 1 -> matching(args[0], actions)
            args.size == 2 && args[0].equals("save", ignoreCase = true) -> hint(args[1], "<name>")
            args.size == 2 -> matching(args[1], TemplateManager.names(player.uniqueId))
            else -> emptyList()
        }
    }

}

class TemplatesSubCommand : SubCommand("templates", Permissions.TEMPLATES, feature = Feature.TEMPLATES) {

    override fun execute(sender: CommandSender, label: String, args: List<String>) {
        val player = sender as Player
        val names = TemplateManager.names(player.uniqueId)
        if (names.isEmpty()) return error(player, "no-templates")

        success(player, "templates-list", Placeholder.unparsed("templates", names.joinToString(", ")))
    }

}
