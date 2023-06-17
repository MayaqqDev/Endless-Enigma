package dev.mayaqq.endless.mixin;

import dev.mayaqq.endless.Endless;
import dev.mayaqq.endless.networking.PacketMethods;
import dev.mayaqq.endless.utils.AdvancementUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.block.Blocks;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

import static dev.mayaqq.endless.Endless.id;

@Mixin(EnderDragonFight.class)
public class EnderDragonFightMixin {
    @Shadow @Final private ServerWorld world;

    @Shadow private boolean previouslyKilled;
    @Final
    @Shadow private BlockPos origin;

    @Inject(method = "dragonKilled", at = @At("HEAD"))
    private void injectDragonKilled(EnderDragonEntity dragon, CallbackInfo ci) {
        AtomicBoolean hasAdvancement = new AtomicBoolean(true);
        if (!this.world.isClient) {
            this.world.getServer().getPlayerManager().getPlayerList().forEach(player -> {
                if (player.getWorld().getRegistryKey() == World.END && !AdvancementUtils.hasAdvancement(player, id("root"))) {
                    hasAdvancement.set(false);
                    PacketMethods.showCutscene(player, "The Dragon egg seems to be emitting a strange energy...");
                    PacketMethods.showCutscene(player, "You should probably check it out...");
                    PlayerAdvancementTracker advancementTracker = player.getAdvancementTracker();
                    Advancement advancement = this.world.getServer().getAdvancementLoader().get(id("root"));
                    advancementTracker.grantCriterion(advancement, "got_first_cutscene");
                }
            });
        }
        if (previouslyKilled && !hasAdvancement.get()) {
            this.world.setBlockState(this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, EndPortalFeature.offsetOrigin(this.origin)), Blocks.DRAGON_EGG.getDefaultState());
        }
    }
}