package me.ivan.gatewayer.mixin;

import me.ivan.gatewayer.Gatewayer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin {
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void onPlayerRightClickBlock(World world, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (world.isClient) {
            ActionResult result = Gatewayer.getInstance().onPlayerRightClickBlock(world, player, hand, hit);
            if (result.isAccepted()) {
                cir.setReturnValue(result);
            }
        }
    }
}
