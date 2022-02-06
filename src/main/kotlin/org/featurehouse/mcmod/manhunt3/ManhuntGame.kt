@file:JvmMultifileClass
@file:JvmName("ManhuntUtils")

package org.featurehouse.mcmod.manhunt3

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtByte
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

/**
 * @return the error message, or null if no errors found.
 * Left=key, Right=default_message
 */
@Throws(CommandSyntaxException::class)
internal fun MinecraftServer.checkValidManhuntGame() {
    if (this.speedrunners.isEmpty())
        //throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
        throw CommandSyntaxException(EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION, Text.of("No speedrunners set"))
        //return TranslatableText("commands.manhunt3.speedrunners.none") to "No speedrunners set"
    if (this.hunters.isEmpty())
        throw CommandSyntaxException(EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION, Text.of("No hunters set"))
        //return TranslatableText("commands.manhunt3.hunters.none") to "No hunters set"

    this.speedrunners.forEach {
        val playerEntity = this.playerManager.getPlayer(it)
        if (playerEntity == null) {
            throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create()
            //return TranslatableText("commands.manhunt3.absent", it) to "Player $it is absent"
        } else if (it in hunters)  {
            /*return TranslatableText(
                "commands.manhunt3.both", playerEntity, it
            ) to "Player $playerEntity" +
                    " (UUID=$it) is set to both a speedrunner and a hunter"*/
            throw CommandSyntaxException(EntityArgumentType.NOT_ALLOWED_EXCEPTION,
                Text.of("Player $playerEntity (UUID=$it) is set to both a speedrunner and a hunter"))
        }
    }
    this.hunters.forEach {
        if (this.playerManager.getPlayer(it) == null) {
            throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create()
            //return TranslatableText("commands.manhunt3.absent", it) to "Player `$it` is absent"
        }
    }
    //return null
}

internal val trackableCompass : ItemStack
    get() = ItemStack(Items.COMPASS).apply {
        orCreateNbt.putBoolean("manhunt3:track", true)
}

internal fun PlayerInventory.giveHunterCompass() {
    for (itemStack in this.main + offHand + armor /* This is damn ridiculous */) {
        if (itemStack.isOf(Items.COMPASS) &&
                    NbtByte.ONE == itemStack.getSubNbt("manhunt3:track")
                ) {
            return
        }
    }
    player.giveItemStack(trackableCompass)
}

internal fun MinecraftServer.startManhuntImpl() {
    speedrunning = true
    playerManager.playerList.forEach {
        it.sendMessage(
            if (it.isModded)
                TranslatableText("game.manhunt3.start").formatted(Formatting.YELLOW, Formatting.BOLD)
            else
                LiteralText("Game Starts!").formatted(Formatting.YELLOW, Formatting.BOLD)
            , false
        )
    }
}

@Throws(CommandSyntaxException::class)
fun startManhunt(ctx: CommandContext<ServerCommandSource>) : Int {
    ctx.source.server.run {
        checkValidManhuntGame()
        if (speedrunning) {
            ctx.source.sendError(
                if (ctx.source.player.isModded)
                    TranslatableText("game.manhunt3.start.already")
                else
                    Text.of("Manhunt Game has already started")
            )
            return 1
        }
        for (hunter in hunters) {
            playerManager.getPlayer(hunter)!!.inventory.giveHunterCompass()
        }
        playerManager.playerList.forEach {
            it.sendMessage(
                if (it.isModded)
                    TranslatableText("game.manhunt3.start.5s").formatted(Formatting.YELLOW)
                else
                    LiteralText("Manhunt Game will start in 5 seconds...").formatted(Formatting.YELLOW)
                , false
            )
        }
        scheduleManhuntGame()
    }
    return 1
}

fun stopManhunt(ctx: CommandContext<ServerCommandSource>) : Int {
    ctx.source.server.run {
        stopManhuntImpl()
        return 1
    }
}

internal fun MinecraftServer.stopManhuntImpl() {
    if (this.speedrunning ||
        (this as ManhuntServer).manhunt3_waitingForManhuntGameStart > 0) {
        // running or scheduling
        speedrunning = false
        (this as ManhuntServer).manhunt3_waitingForManhuntGameStart = 0

        playerManager.playerList.forEach {
            it.sendMessage(
                if (it.isModded)
                    TranslatableText("game.manhunt3.stop").formatted(Formatting.RED, Formatting.BOLD)
                else
                    LiteralText("Manhunt Game Stops!").formatted(Formatting.RED, Formatting.BOLD)
                , false)
        }
    }
}

fun hunterKilledDragon(hunter: PlayerEntity, server: MinecraftServer) {
    server.playerManager.playerList.forEach {
        it.sendMessage(
            if (it.isModded)
                TranslatableText("game.manhunt3.dragon.hunter", hunter.gameProfile.name).formatted(Formatting.RED, Formatting.BOLD)
            else
                LiteralText("Hunter ${hunter.gameProfile.name} killed the dragon!").formatted(Formatting.RED, Formatting.BOLD)
            , false)
    }
    server.stopManhuntImpl()
}

fun speedrunnerKilledDragon(speedrunner: PlayerEntity, server: MinecraftServer) {
    server.playerManager.playerList.forEach {
        it.sendMessage(
            if (it.isModded)
                TranslatableText("game.manhunt3.dragon.speedrunner", speedrunner.gameProfile.name).formatted(Formatting.GREEN, Formatting.BOLD)
            else
                LiteralText("Speedrunner ${speedrunner.gameProfile.name} killed the dragon!").formatted(Formatting.GREEN, Formatting.BOLD)
            , false)
    }
    server.stopManhuntImpl()
}
