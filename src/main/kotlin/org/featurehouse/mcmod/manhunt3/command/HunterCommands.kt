//@file:JvmName("CommandsKt")
//@file:JvmMultifileClass

package org.featurehouse.mcmod.manhunt3.command

import com.mojang.brigadier.context.CommandContext
import joptsimple.internal.Strings
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import org.featurehouse.mcmod.manhunt3.hunters
import org.featurehouse.mcmod.manhunt3.isHunter
import org.featurehouse.mcmod.manhunt3.isModded
import org.featurehouse.mcmod.manhunt3.isSpeedRunner
import java.util.stream.Stream

fun getHunters(ctx: CommandContext<ServerCommandSource>) : Int {
    val source: ServerCommandSource = ctx.source
    val huntersUUID = source.server.hunters
    val hunters: List<String> = huntersUUID.stream().flatMap {
        source.server.playerManager.getPlayer(it).run {
            return@flatMap if (this == null)
                Stream.empty()
            else Stream.of("- ${this.gameProfile.name}")
        }
    }.toList()
    if (hunters.isEmpty()) {
        source.sendFeedback(if (source.player.isModded)
            TranslatableText("commands.manhunt3.hunters.none")
        else Text.of("No hunters set"), false)
    } else {
        val connectedText: String = Strings.join(hunters, "\n")
        source.sendFeedback(
            if (source.player.isModded)
                TranslatableText("commands.manhunt3.hunters.get", connectedText)
            else LiteralText("Hunters are:\n$connectedText")
            , false)
    }
    return 1
}

fun addHunter(ctx: CommandContext<ServerCommandSource>) : Int {
    val source = ctx.source
    val hunters : ServerPlayerEntity = EntityArgumentType.getPlayer(ctx, "player")
    when {
        hunters.isSpeedRunner() ->
            source.sendError(
                if (source.player.isModded)
                    TranslatableText("commands.manhunt3.speedrunners.already",
                        hunters.gameProfile.name)
                else Text.of(hunters.gameProfile.name +
                        " is already a speedrunner")
            )
        hunters.isHunter() ->
            source.sendError(
                if (source.player.isModded)
                    TranslatableText("commands.manhunt3.hunters.already",
                        hunters.gameProfile.name)
                else Text.of(hunters.gameProfile.name +
                        " is already a hunter")
            )
        else -> {
            source.server.hunters.add(hunters.uuid)
            source.sendFeedback(
                if (source.player.isModded)
                    TranslatableText("commands.manhunt3.hunters.add",
                        hunters.gameProfile.name)
                else Text.of(hunters.gameProfile.name +
                        " is now a hunters")
                , true)
        }
    }
    return 1
}

fun clearHunters(ctx: CommandContext<ServerCommandSource>) : Int {
    val source = ctx.source
    source.server.hunters.clear()
    source.sendFeedback(
        if (source.player.isModded)
            TranslatableText("commands.manhunt3.hunters.clear")
        else Text.of("Hunters are cleared")
        , true)
    return 1
}

fun removeHunter(ctx: CommandContext<ServerCommandSource>) : Int {
    val source = ctx.source
    val hunter : ServerPlayerEntity = EntityArgumentType.getPlayer(ctx, "player")
    if (source.server.hunters.remove(hunter.uuid)) {
        source.sendFeedback(
            if (source.player.isModded)
                TranslatableText("commands.manhunt3.hunters.remove", hunter)
            else Text.of("$hunter is removed from hunter list")
            , true)
    } else {
        source.sendError(
            if (source.player.isModded)
                TranslatableText("commands.manhunt3.hunters.remove.fail")
            else Text.of("$hunter was not a hunter yet")
        )
    }
    return 1
}