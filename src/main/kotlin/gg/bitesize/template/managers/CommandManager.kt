package gg.bitesize.template.managers

import gg.bitesize.template.Main.Companion.INSTANCE
import gg.bitesize.template.commands.BaseCommand
import org.bukkit.Bukkit

object CommandManager {

    private val commandMap = Bukkit.getServer().commandMap
    private val knownCommands = commandMap.knownCommands
    private val registeredCommands = mutableSetOf<BaseCommand>()

    fun registerAll() {

    }

    fun register(command: BaseCommand) {
        commandMap.register(command.name.lowercase(), command)
        registeredCommands.add(command)
    }

    fun unregisterVanilla(commandName: String) {
        val command = knownCommands[commandName] ?: return

        command.unregister(commandMap)
        knownCommands.remove(commandName)
        command.aliases.forEach { alias ->
            if(knownCommands[alias] == command) {
                knownCommands.remove(alias)
            }
        }

        knownCommands.remove("minecraft:$commandName")
        knownCommands.remove("bukkit:$commandName")
    }

    fun removeVanillaCommands() {
        var vanillaCommands = ConfigManager.getList("remove-vanilla-commands")
        vanillaCommands.forEach { command ->
            unregisterVanilla(command)
        }

        INSTANCE.logger.info("Removed ${vanillaCommands.size} vanilla commands.")
    }

    fun syncCommands() {
        Bukkit.getServer().onlinePlayers.forEach { player ->
            player.updateCommands()
        }
    }

    fun shutdown() {
        registeredCommands.forEach { command ->
            command.unregister(commandMap)

            knownCommands.remove(command.name)
            knownCommands.remove("${INSTANCE.name.lowercase()}:${command.name}")
            command.aliases.forEach { alias ->
                knownCommands.remove(alias)
                knownCommands.remove("${INSTANCE.name.lowercase()}:$alias")
            }
        }

        registeredCommands.clear()

        INSTANCE.logger.info("Unregistered commands.")
    }

}