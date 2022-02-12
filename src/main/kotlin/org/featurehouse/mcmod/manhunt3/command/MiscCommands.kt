//@file:JvmName("CommandsKt")
//@file:JvmMultifileClass

package org.featurehouse.mcmod.manhunt3.command

import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import org.featurehouse.mcmod.manhunt3.isModded
import org.featurehouse.mcmod.manhunt3.moddedPlayers
import org.featurehouse.mcmod.manhunt3.serverQuestPackId

fun clearMHCache(ctx: CommandContext<ServerCommandSource>) : Int {
    val server : MinecraftServer = ctx.source.server
    server.moddedPlayers.clear()
    server.playerManager.playerList.run {
        forEach {
            ServerPlayNetworking.send(it, serverQuestPackId, PacketByteBufs.create().apply {
                writeByte(77)
            })
        }
        ctx.source.sendFeedback(
            if (ctx.source.player.isModded)
                TranslatableText("commands.manhunt3.cache.clear", size)
            else Text.of("Resending mod installation checking packet" +
                    "to $size player(s)")
        , true)
    }
    return 1
}
