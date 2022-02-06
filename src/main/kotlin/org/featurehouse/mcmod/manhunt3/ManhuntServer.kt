package org.featurehouse.mcmod.manhunt3

import net.minecraft.server.network.ServerPlayerEntity

interface ManhuntServer {
    var manhunt3_waitingForManhuntGameStart : Int
    val manhunt3_moddedPlayers: MutableCollection<ServerPlayerEntity>
}