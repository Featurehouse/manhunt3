package org.featurehouse.mcmod.manhunt3.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.featurehouse.mcmod.manhunt3.ManhuntServer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collection;
import java.util.LinkedHashSet;

@Mixin(MinecraftServer.class)
class ManhuntServerMixin implements ManhuntServer {
    @Unique private final Collection<ServerPlayerEntity> manhunt3_moddedPlayers
            = new LinkedHashSet<>();
    @Unique private int manhunt3_waitingForManhuntGameStart;

    @NotNull
    @Override
    public Collection<ServerPlayerEntity> getManhunt3_moddedPlayers() {
        return manhunt3_moddedPlayers;
    }

    @Override
    public int getManhunt3_waitingForManhuntGameStart() {
        return manhunt3_waitingForManhuntGameStart;
    }

    @Override
    public void setManhunt3_waitingForManhuntGameStart(int i) {
        manhunt3_waitingForManhuntGameStart = i;
    }
}
