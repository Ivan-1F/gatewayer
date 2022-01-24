package me.ivan.gatewayer.mixin;

import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EndGatewayBlockEntity.class)
public interface EndGatewayBlockEntityAccessor {
    @Accessor("exitPortalPos")
    public BlockPos getExitPortalPos();
    @Accessor("exactTeleport")
    public boolean getExactTeleport();
    @Accessor("teleportCooldown")
    public int getTeleportCooldown();
}
