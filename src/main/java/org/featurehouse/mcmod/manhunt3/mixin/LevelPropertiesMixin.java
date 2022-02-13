package org.featurehouse.mcmod.manhunt3.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.SaveVersionInfo;
import org.featurehouse.mcmod.manhunt3.ManhuntServerProperties;
import org.featurehouse.mcmod.manhunt3.ManhuntUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(LevelProperties.class)
public class LevelPropertiesMixin implements ManhuntServerProperties {
    @Unique private final ManhuntServerProperties manhunt3_prop = new Impl();

    @Inject(method = "readProperties", at = @At("RETURN"))
    private static void readProperties(Dynamic<NbtElement> dynamic, DataFixer dataFixer, int dataVersion, NbtCompound playerData, LevelInfo levelInfo, SaveVersionInfo saveVersionInfo, GeneratorOptions generatorOptions, Lifecycle lifecycle,
                                       CallbackInfoReturnable<LevelProperties> cir) {
        dynamic.get("manhunt3:data").result().ifPresentOrElse(data -> {
            Function<Dynamic<NbtElement>, Stream<UUID>> func = dyn -> {
                try {
                    return Stream.of(NbtHelper.toUuid(dyn.getValue()));
                } catch (IllegalArgumentException e) {
                    ManhuntUtils.getLogger().warn("Failed to process UUID: " + dyn, e);
                    return Stream.empty();
                }
            };
            ManhuntServerProperties prop = (ManhuntServerProperties) cir.getReturnValue();
            prop.setManhunt3_speedrunning(data.get("Speedrunning").asBoolean(false));
            prop.setManhunt3_hunters(data.get("Hunters").asStream().flatMap(func).collect(Collectors.toSet()));
            prop.setManhunt3_speedrunners(data.get("Speedrunners").asStream().flatMap(func).collect(Collectors.toSet()));
        }, () -> {});
    }

    @Inject(method = "updateProperties", at = @At("RETURN"))
    private void writeToNbt(DynamicRegistryManager registryManager, NbtCompound levelNbt, NbtCompound playerNbt, CallbackInfo ci) {
        NbtCompound m3data = new NbtCompound();
        levelNbt.put("manhunt3:data", m3data);
        m3data.putBoolean("Speedrunning", this.getManhunt3_speedrunning());
        m3data.put("Hunters", this.getManhunt3_hunters().stream().map(NbtHelper::fromUuid)
                .collect(Collectors.toCollection(NbtList::new)));
        m3data.put("Speedrunners", this.getManhunt3_speedrunners().stream().map(NbtHelper::fromUuid)
                .collect(Collectors.toCollection(NbtList::new)));
    }

    @NotNull
    @Override
    public Set<UUID> getManhunt3_speedrunners() {
        return manhunt3_prop.getManhunt3_speedrunners();
    }

    @Override
    public void setManhunt3_speedrunners(@NotNull Set<UUID> speedrunners) {
        manhunt3_prop.setManhunt3_speedrunners(speedrunners);
    }

    @NotNull
    @Override
    public Set<UUID> getManhunt3_hunters() {
        return manhunt3_prop.getManhunt3_hunters();
    }

    @Override
    public void setManhunt3_hunters(@NotNull Set<UUID> hunters) {
        manhunt3_prop.setManhunt3_hunters(hunters);
    }

    @Override
    public boolean getManhunt3_speedrunning() {
        return manhunt3_prop.getManhunt3_speedrunning();
    }

    @Override
    public void setManhunt3_speedrunning(boolean speedrunning) {
        manhunt3_prop.setManhunt3_speedrunning(speedrunning);
    }
}