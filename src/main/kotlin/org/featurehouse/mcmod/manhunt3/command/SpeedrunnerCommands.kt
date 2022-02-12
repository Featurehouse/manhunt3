//@file:JvmName("CommandsKt")
//@file:JvmMultifileClass

package org.featurehouse.mcmod.manhunt3.command

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import joptsimple.internal.Strings
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import org.featurehouse.mcmod.manhunt3.*
import org.featurehouse.mcmod.manhunt3.isModded
import java.util.stream.Stream

internal fun <T : ArgumentBuilder<ServerCommandSource, T>> ArgumentBuilder<ServerCommandSource, T>
        .requiresPermissionLevel(lvl: Int) : ArgumentBuilder<ServerCommandSource, T>{
    return this.requires { it.hasPermissionLevel(lvl) }
}

fun getSpeedrunners(ctx: CommandContext<ServerCommandSource>) : Int {
    val source: ServerCommandSource = ctx.source
    val speedrunnersUUID = source.server.speedrunners
    val speedrunners: List<String> = speedrunnersUUID.stream().flatMap {
        source.server.playerManager.getPlayer(it).run {
            return@flatMap if (this == null)
                Stream.empty()
            else Stream.of("- ${this.gameProfile.name}")
        }
    }.toList()
    if (speedrunners.isEmpty()) {
        source.sendFeedback(if (source.player.isModded)
            TranslatableText("commands.manhunt3.speedrunners.none")
            else Text.of("No speedrunners set"), false)
    } else {
        val connectedText: String = Strings.join(speedrunners, "\n")
        source.sendFeedback(
            if (source.player.isModded)
                TranslatableText("commands.manhunt3.speedrunners.get", connectedText)
            else LiteralText("Speedrunners are:\n$connectedText")
        , false)
    }
    return 1
}

fun addSpeedrunner(ctx: CommandContext<ServerCommandSource>) : Int {
    val source = ctx.source
    val speedrunner : ServerPlayerEntity = EntityArgumentType.getPlayer(ctx, "player")
    when {
        speedrunner.isSpeedRunner() ->
            source.sendError(
                if (source.player.isModded)
                    TranslatableText("commands.manhunt3.speedrunners.already",
                    speedrunner.gameProfile.name)
                else Text.of(speedrunner.gameProfile.name +
                        " is already a speedrunner")
            )
        speedrunner.isHunter() ->
            source.sendError(
                if (source.player.isModded)
                    TranslatableText("commands.manhunt3.hunters.already",
                    speedrunner.gameProfile.name)
                else Text.of(speedrunner.gameProfile.name +
                        " is already a hunter")
            )
        else -> {
            source.server.speedrunners.add(speedrunner.uuid)
            source.sendFeedback(
                if (source.player.isModded)
                    TranslatableText("commands.manhunt3.speedrunners.add",
                    speedrunner.gameProfile.name)
                else Text.of(speedrunner.gameProfile.name +
                        " is now a speedrunner")
            , true)
        }
    }
    return 1
}

fun clearSpeedrunner(ctx: CommandContext<ServerCommandSource>) : Int {
    val source = ctx.source
    source.server.speedrunners.clear()
    source.sendFeedback(
        if (source.player.isModded)
            TranslatableText("commands.manhunt3.speedrunners.clear")
        else Text.of("Speedrunners are cleared")
    , true)
    return 1
}

fun removeSpeedrunner(ctx: CommandContext<ServerCommandSource>) : Int {
    val source = ctx.source
    val speedrunner : ServerPlayerEntity = EntityArgumentType.getPlayer(ctx, "player")
    if (source.server.speedrunners.remove(speedrunner.uuid)) {
        source.sendFeedback(
            if (source.player.isModded)
                TranslatableText("commands.manhunt3.speedrunners.remove", speedrunner)
            else Text.of("$speedrunner is removed from speedrunner list")
        , true)
    } else {
        source.sendError(
            if (source.player.isModded)
                TranslatableText("commands.manhunt3.speedrunners.remove.fail")
            else Text.of("$speedrunner was not a speedrunner yet")
        )
    }
    return 1
}
