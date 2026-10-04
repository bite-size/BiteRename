package gg.bitesize.rename.managers

import gg.bitesize.rename.BiteRename.Companion.INSTANCE
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.CommandSender

object FormatManager {

    private val miniMessage = MiniMessage.miniMessage()

    // Section-sign output with hex works on Spigot, Paper, Purpur, and the console
    private val legacySerializer = LegacyComponentSerializer.builder()
        .character(LegacyComponentSerializer.SECTION_CHAR)
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build()

    // Bitesize palette, exposed as <primary>, <secondary>, ... and overridable in messages.yml
    private val THEME_DEFAULTS = linkedMapOf(
        "primary" to "#FF0368",
        "secondary" to "#4ECDC4",
        "light" to "#F7FFF7",
        "muted" to "#AEADF0",
        "dark" to "#011627",
    )

    private var theme: TagResolver = TagResolver.empty()
    private var prefix: Component = Component.empty()

    fun reload() {
        theme = TagResolver.resolver(THEME_DEFAULTS.map { (name, fallback) ->
            Placeholder.styling(name, resolveColor(name, fallback))
        })
        prefix = parseMiniMessage(ConfigManager.getPrefix())
    }

    private fun resolveColor(name: String, fallback: String): TextColor {
        val configured = ConfigManager.messages.getString("colors.$name")
        configured?.let(TextColor::fromHexString)?.let { return it }

        if (configured != null) {
            INSTANCE.logger.warning("Invalid color '$configured' at colors.$name in messages.yml. Using $fallback.")
        }
        return TextColor.fromHexString(fallback)!!
    }

    fun parseMiniMessage(text: String): Component {
        return miniMessage.deserialize(text, theme)
    }

    fun parseMiniMessage(text: String, vararg placeholders: TagResolver): Component {
        return miniMessage.deserialize(text, theme, *placeholders)
    }

    fun toLegacy(component: Component): String {
        return legacySerializer.serialize(component)
    }

    fun getPrefix(): Component {
        return prefix
    }

    fun send(sender: CommandSender, text: String, usePrefix: Boolean = true,
             vararg placeholders: TagResolver) {

        var message = parseMiniMessage(text, *placeholders)

        if (usePrefix) {
            // Siblings, not a child, so the prefix's styling can't leak into the message
            message = Component.text().append(prefix, message).build()
        }

        sender.sendMessage(toLegacy(message))

    }

    fun sendList(sender: CommandSender, lines: List<String>, vararg placeholders: TagResolver) {
        lines.forEach { line -> send(sender, line, false, *placeholders) }
    }

    fun error(sender: CommandSender, text: String, vararg placeholders: TagResolver) {
        send(sender, ConfigManager.getErrorPrefix() + text, true, *placeholders)
    }

}
