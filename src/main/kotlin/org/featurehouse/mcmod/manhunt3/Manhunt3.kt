package org.featurehouse.mcmod.manhunt3

import com.google.common.collect.Iterables
import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.Vec3d
import org.featurehouse.mcmod.manhunt3.command.*
import java.util.stream.Stream
import kotlin.math.roundToInt

object Manhunt3 : ModInitializer {
    const val modId = "manhunt3"

    override fun onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(id("schedule_manhunt_start")) { server ->
            (server as ManhuntServer).run {
                if (manhunt3_waitingForManhuntGameStart > 0)
                    manhunt3_waitingForManhuntGameStart--
                else if (!server.speedrunning)
                    server.startManhuntImpl()
            }
        }

        UseItemCallback.EVENT.register(id("use_compass")) { player, world, hand ->
            val stack = player.getStackInHand(hand)
            return@register if (!world.isClient) {
                val server = world.server!!
                if (!server.speedrunning || !player.isHunter()) {
                    TypedActionResult.pass(stack)
                } else {
                    val stream: Stream<PlayerEntity> = server.speedrunners.parallelStream().flatMap {
                        val player0 = world.getPlayerByUuid(it)
                        if (player0 == null) Stream.empty<PlayerEntity>()
                        Stream.of(player0)
                    }
                    val pos: Vec3d? = getNearestPlayerPos(player.pos, stream)
                    if (pos != null) {
                        stack.orCreateNbt.run {
                            putBoolean("LodestoneTracked", false)
                            putString("LodestoneDimension", world.registryKey.value.toString())
                            put("LodestonePos", NbtCompound().apply {
                                putInt("X", pos.x.roundToInt())
                                putInt("Y", pos.y.toInt())
                                putInt("Z", pos.z.roundToInt())
                            })
                            putBoolean("manhunt3:track", true)
                        }
                        TypedActionResult.consume(stack)
                    } else TypedActionResult.pass(stack)
                }
            } else TypedActionResult.success(stack)
        }

        ServerPlayConnectionEvents.INIT.register(serverQuestPackId) { handler: ServerPlayNetworkHandler, _ ->
            ServerPlayNetworking.send(handler.player, serverQuestPackId, PacketByteBufs.empty())
        }

        ServerPlayNetworking.registerGlobalReceiver(clientAnswerPackId) { server, player, _, _, _ ->
            server.moddedPlayers.add(player)
            // TODO: when buf contains bytes `77`, it means the request was sent by a command
        }

        CommandRegistrationCallback.EVENT.register(id("commands")) { dispatcher, _ ->
            dispatcher.register(literal<ServerCommandSource>("manhunt3")
                .then(
                    literal<ServerCommandSource>("speedrunners")
                        .then(literal<ServerCommandSource>("get")
                            .executes(::getSpeedrunners))
                        .then(literal<ServerCommandSource>("add").requiresPermissionLevel(2)
                            .executes(::addSpeedrunner)).then(argument("player", EntityArgumentType.player()))
                        .then(literal<ServerCommandSource>("clear").requiresPermissionLevel(2)
                            .executes(::clearSpeedrunner))
                        .then(literal<ServerCommandSource>("remove").requiresPermissionLevel(2)
                            .executes(::removeSpeedrunner)).then(argument("player", EntityArgumentType.player()))
                ).then(
                    literal<ServerCommandSource>("hunters")
                        .then(literal<ServerCommandSource>("get")
                            .executes(::getHunters))
                        .then(literal<ServerCommandSource>("add").requiresPermissionLevel(2)
                            .executes(::addHunter)).then(argument("player", EntityArgumentType.player()))
                        .then(literal<ServerCommandSource>("clear").requiresPermissionLevel(2)
                            .executes(::clearHunters))
                        .then(literal<ServerCommandSource>("remove").requiresPermissionLevel(2)
                            .executes(::removeHunter)).then(argument("player", EntityArgumentType.player()))
                ).then(
                    literal<ServerCommandSource>("clearCache").requiresPermissionLevel(2)
                        .executes(::clearMHCache)
                ).then(
                    literal<ServerCommandSource>("help")
                        .redirect(dispatcher.root.getChild("help").getChild("manhunt3"))
                        .then(argument<ServerCommandSource, String>("command", StringArgumentType.greedyString())
                            .executes { context ->
                                val parseResults: ParseResults<ServerCommandSource>
                                    = dispatcher.parse(StringArgumentType.getString(context, "command"), context.source)
                                if (parseResults.context.nodes.isEmpty())
                                    throw SimpleCommandExceptionType(TranslatableText("commands.help.failed")).create()
                                val map = dispatcher.getSmartUsage(
                                    Iterables.getLast(parseResults.context.nodes).node,
                                    context.source
                                )
                                for (string in map.values) {
                                    context.source.sendFeedback(LiteralText("/manhunt3 ${parseResults.reader.string} $string"), false)
                                }

                                map.size
                            })
                ).then(
                    literal<ServerCommandSource>("start").requiresPermissionLevel(2)
                        .executes(::startManhunt)
                ).then(
                    literal<ServerCommandSource>("stop").requiresPermissionLevel(2)
                        .executes(::stopManhunt)
                )
            )
        }
    }
}

@Environment(EnvType.CLIENT)
@Suppress("UNUSED")
object ManhuntClient : ClientModInitializer {
    private val loadedResourceLoader : Boolean = FabricLoader.getInstance().isModLoaded("fabric-resource-loader-v0")

    override fun onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(serverQuestPackId) { _, _, buf: PacketByteBuf, sender ->
            if (loadedResourceLoader) {
                sender.sendPacket(clientAnswerPackId, buf)
            }
        }
    }
}