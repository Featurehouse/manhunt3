package org.featurehouse.mcmod.manhunt3.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.featurehouse.mcmod.manhunt3.ManhuntUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
class PlayerEntityMixin {
    @Shadow @Final private PlayerInventory inventory;

    @Inject(at = @At("RETURN"), method = "dropInventory()V")
    @SuppressWarnings("all")
    private void hunter_giveCompass(CallbackInfo ci) {
        if (((Object)this) instanceof ServerPlayerEntity serverPlayer &&
                ManhuntUtils.isHunter(serverPlayer)) {
            ManhuntUtils.giveHunterCompass(this.inventory);
        }
    }

    @Inject(at = @At("RETURN"), method = "onKilledOther")
    private void killDragon(ServerWorld world, LivingEntity other, CallbackInfo ci) {
        if (other instanceof EnderDragonEntity) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            if (ManhuntUtils.isHunter(player))
                ManhuntUtils.hunterKilledDragon(player, world.getServer());
            else if (ManhuntUtils.isSpeedRunner(player))
                ManhuntUtils.speedrunnerKilledDragon(player, world.getServer());
        }
    }
}
