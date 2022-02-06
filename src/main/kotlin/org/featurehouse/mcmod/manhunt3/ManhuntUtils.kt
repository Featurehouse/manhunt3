@file:JvmMultifileClass
@file:JvmName("ManhuntUtils")

package org.featurehouse.mcmod.manhunt3

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import java.util.*
import java.util.stream.Stream

var MinecraftServer.speedrunners: MutableSet<UUID>
    get() = (this.saveProperties.mainWorldProperties as ManhuntServerProperties).manhunt3_speedrunners
    set(value) { (this.saveProperties.mainWorldProperties as ManhuntServerProperties).manhunt3_speedrunners = value }
var MinecraftServer.hunters: MutableSet<UUID>
    get() = (this.saveProperties.mainWorldProperties as ManhuntServerProperties).manhunt3_hunters
    set(value) { (this.saveProperties.mainWorldProperties as ManhuntServerProperties).manhunt3_hunters = value }
var MinecraftServer.speedrunning: Boolean
    get() = (this.saveProperties.mainWorldProperties as ManhuntServerProperties).manhunt3_speedrunning
    set(value) { (this.saveProperties.mainWorldProperties as ManhuntServerProperties).manhunt3_speedrunning = value }

fun PlayerEntity.isSpeedRunner() : Boolean
        = this.uuid in server!!.speedrunners
fun PlayerEntity.isHunter() : Boolean
        = this.uuid in server!!.hunters

fun MinecraftServer.scheduleManhuntGame() {
    (this as ManhuntServer).run {
        if (!speedrunning && manhunt3_waitingForManhuntGameStart <= 0) {
            manhunt3_waitingForManhuntGameStart = 5
        }
    }
}

fun getNearestPlayerPos(vec3: Vec3d, stream: Stream<PlayerEntity>) : Vec3d? {
    return stream.min { p1, p2 ->
        vec3.distanceTo(p1.pos).compareTo(vec3.distanceTo(p2.pos))
    }.orElse(null)?.pos
}

val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger("Manhunt III")

internal val MinecraftServer.moddedPlayers : MutableCollection<ServerPlayerEntity>
        get() = (this as ManhuntServer).manhunt3_moddedPlayers
internal val ServerPlayerEntity.isModded
        get() = this in this.server.moddedPlayers

internal val clientAnswerPackId : Identifier = id("client_answer")
internal val serverQuestPackId: Identifier = id("server_quest")

internal fun id(id: String) = Identifier(Manhunt3.modId, id)

