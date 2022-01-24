package me.ivan.gatewayer.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GatewayInfo {
    private BlockPos pos;
    private World world;

    public GatewayInfo(BlockPos pos, World world) {
        this.pos = pos;
        this.world = world;
    }

    public GatewayInfo() {
        disable();
    }

    public BlockPos getPos() {
        return pos;
    }

    public World getWorld() {
        return world;
    }

    public boolean isEnabled() {
        return this.pos != null;
    }

    public void disable() {
        this.pos = null;
        this.world = null;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public BlockState getBlockState() {
        return this.isEnabled() ? world.getBlockState(this.pos) : null;
    }

    public Block getBlock() {
        if (!this.isEnabled()) return null;
        return this.getBlockState().getBlock();
    }

    public boolean hasBlockEntity() {
        if (!this.isEnabled()) return false;
        return this.getWorld().getBlockEntity(this.getPos()) != null;
    }

    public EndGatewayBlockEntity getBlockEntity() {
        if (!this.isEnabled() || !this.hasBlockEntity()) return null;
        BlockEntity blockEntity = this.world.getBlockEntity(this.pos);
        EndGatewayBlockEntity gateway = (EndGatewayBlockEntity) blockEntity;
        return gateway;
    }

    public BlockPos getEntityExitPos() {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        for (int i = 255; i >= 0; i--) {
            for (int j = x - 5; j <= x + 5; j++) {
                for (int k = z - 5; k <= z + 5; k++) {
                    BlockPos pos = new BlockPos(j, i, k);
                    BlockState blockState = world.getBlockState(pos);
                    Block block = blockState.getBlock();
                    if (blockState.isFullCube(world, pos) && block != Blocks.BEDROCK) {
                        return pos;
                    }
                }
            }
        }

        return this.pos;
    }

}
