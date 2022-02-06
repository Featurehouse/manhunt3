package org.featurehouse.mcmod.manhunt3

import java.util.*

interface ManhuntServerProperties {
    var manhunt3_speedrunners: MutableSet<UUID>
    var manhunt3_hunters: MutableSet<UUID>

    var manhunt3_speedrunning: Boolean

    data class Impl(override var manhunt3_speedrunners: MutableSet<UUID> = mutableSetOf(),
                    override var manhunt3_hunters: MutableSet<UUID> = mutableSetOf(),
                    override var manhunt3_speedrunning: Boolean = false) : ManhuntServerProperties
}