package gg.bitesize.template.managers

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

object FormatManager {

    private val miniMessage = MiniMessage.miniMessage()

    fun parseMiniMessage(text: String): Component {
        return miniMessage.deserialize(text)
    }

    fun parseMiniMessage(text: String, vararg placeholders: TagResolver): Component {
        return miniMessage.deserialize(text, *placeholders)
    }

    fun getPrefix(): Component {
        return parseMiniMessage(ConfigManager.getString("prefix"))
    }

    fun send(audience: Audience, text: String, usePrefix: Boolean = true,
             vararg placeholders: TagResolver) {

        var message = parseMiniMessage(text, *placeholders)

        if (usePrefix) {
            message = getPrefix().append(message)
        }

        audience.sendMessage(message)

    }

    fun error(audience: Audience, text: String, vararg placeholders: TagResolver) {
        send(audience, ConfigManager.getErrorPrefix() + text, true, *placeholders)
    }

}