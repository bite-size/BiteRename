package gg.bitesize.rename.items

import gg.bitesize.rename.managers.FormatManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

/**
 * Turns player-written text into item names and lore.
 * Accepts MiniMessage and legacy & codes, including mixed in one string.
 */
object ItemText {

    // Visual tags only: interactive or server-resolved tags (click, hover, selector, nbt, ...) never reach an item
    private val miniMessage = MiniMessage.builder()
        .tags(TagResolver.resolver(
            StandardTags.color(),
            StandardTags.decorations(),
            StandardTags.gradient(),
            StandardTags.rainbow(),
            StandardTags.transition(),
            StandardTags.pride(),
            StandardTags.reset(),
        ))
        .build()

    private val plainSerializer = PlainTextComponentSerializer.plainText()

    // &#RRGGBB, &x&R&R&G&G&B&B, or a single &-code
    private val LEGACY_PATTERN = Regex("&(?:#([0-9a-fA-F]{6})|[xX]((?:&[0-9a-fA-F]){6})|([0-9a-fA-Fk-oK-OrR]))")

    // A legacy color also clears formatting, so colors are preceded by <reset>
    private val LEGACY_TAGS = mapOf(
        '0' to "<reset><black>", '1' to "<reset><dark_blue>", '2' to "<reset><dark_green>",
        '3' to "<reset><dark_aqua>", '4' to "<reset><dark_red>", '5' to "<reset><dark_purple>",
        '6' to "<reset><gold>", '7' to "<reset><gray>", '8' to "<reset><dark_gray>",
        '9' to "<reset><blue>", 'a' to "<reset><green>", 'b' to "<reset><aqua>",
        'c' to "<reset><red>", 'd' to "<reset><light_purple>", 'e' to "<reset><yellow>",
        'f' to "<reset><white>", 'k' to "<obfuscated>", 'l' to "<bold>",
        'm' to "<strikethrough>", 'n' to "<underlined>", 'o' to "<italic>", 'r' to "<reset>",
    )

    // CraftBukkit maps a leading reset to an explicitly non-italic style
    private const val UPRIGHT = "§r"

    fun parse(input: String): Component {
        return miniMessage.deserialize(translateLegacy(input))
    }

    fun plainText(component: Component): String {
        return plainSerializer.serialize(component)
    }

    fun toItemString(component: Component, upright: Boolean): String {
        return upright(FormatManager.toLegacy(component), upright)
    }

    fun upright(legacy: String, upright: Boolean): String {
        return if (upright && legacy.isNotEmpty() && !legacy.startsWith(UPRIGHT)) UPRIGHT + legacy else legacy
    }

    fun upright(lines: List<String>, upright: Boolean): List<String> {
        return if (upright) lines.map { upright(it, true) } else lines
    }

    fun translateLegacy(input: String): String {
        if ('&' !in input) return input

        return LEGACY_PATTERN.replace(input) { match ->
            val (hex, spigotHex, code) = match.destructured
            when {
                hex.isNotEmpty() -> "<reset><#$hex>"
                spigotHex.isNotEmpty() -> "<reset><#${spigotHex.replace("&", "")}>"
                else -> LEGACY_TAGS.getValue(code.lowercase()[0])
            }
        }
    }

}
