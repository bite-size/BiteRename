package gg.bitesize.rename.managers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
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

    fun parseMiniMessage(text: String): Component {
        return miniMessage.deserialize(text)
    }

    fun parseMiniMessage(text: String, vararg placeholders: TagResolver): Component {
        return miniMessage.deserialize(text, *placeholders)
    }

    fun toLegacy(component: Component): String {
        return legacySerializer.serialize(component)
    }

    fun getPrefix(): Component {
        return parseMiniMessage(ConfigManager.getPrefix())
    }

    fun send(sender: CommandSender, text: String, usePrefix: Boolean = true,
             vararg placeholders: TagResolver) {

        var message = parseMiniMessage(text, *placeholders)

        if (usePrefix) {
            // Siblings, not a child, so the prefix's styling can't leak into the message
            message = Component.text().append(getPrefix(), message).build()
        }

        sender.sendMessage(toLegacy(message))

    }

    fun error(sender: CommandSender, text: String, vararg placeholders: TagResolver) {
        send(sender, ConfigManager.getErrorPrefix() + text, true, *placeholders)
    }

}
