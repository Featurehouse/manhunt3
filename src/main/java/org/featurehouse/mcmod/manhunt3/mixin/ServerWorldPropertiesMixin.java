package org.featurehouse.mcmod.manhunt3.mixin;

import net.minecraft.world.level.ServerWorldProperties;
import org.featurehouse.mcmod.manhunt3.ManhuntServerProperties;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Set;
import java.util.UUID;

@Mixin(ServerWorldProperties.class)
public interface ServerWorldPropertiesMixin extends ManhuntServerProperties {
    @Override
    default boolean getManhunt3_speedrunning() { return false; }
    @Override
    default void setManhunt3_speedrunning(boolean manhunt3_speedrunning) {}
    @NotNull
    @Override
    default Set<UUID> getManhunt3_hunters() { return Set.of(); }
    @Override
    default void setManhunt3_hunters(@NotNull Set<UUID> manhunt3_hunters) {}
    @NotNull @Override
    default Set<UUID> getManhunt3_speedrunners() {return Set.of(); }
    @Override
    default void setManhunt3_speedrunners(@NotNull Set<UUID> manhunt3_speedrunners) {}
}