package me.ivan.gatewayer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ivan.gatewayer.mixin.EndGatewayBlockEntityAccessor;
import me.ivan.gatewayer.utils.GatewayInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndGatewayBlock;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.Rotation3;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Gatewayer {
    private static final Gatewayer INSTANCE = new Gatewayer();
    private static final double MAX_RENDER_DISTANCE = 256.0D;
    private static final float FONT_SIZE = 0.025F;
    private GatewayInfo gatewayInfo = new GatewayInfo();

    public static Gatewayer getInstance() {
        return INSTANCE;
    }

    public ActionResult onPlayerRightClickBlock(World world, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.MAIN_HAND && player.getMainHandStack().isEmpty() && !player.isSneaking()) {
            BlockPos blockPos = hit.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);
            Block block = blockState.getBlock();
            if (blockPos.equals(gatewayInfo.getPos())) {
                gatewayInfo.disable();
            } else {
                if (block instanceof EndGatewayBlock) {
                    gatewayInfo.setPos(blockPos);
                    gatewayInfo.setWorld(world);
                    return ActionResult.SUCCESS;
                }
            }
        }
        return ActionResult.FAIL;
    }

    public void renderInfo(float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();
        if (gatewayInfo.isEnabled() && camera.getPos().squaredDistanceTo(new Vec3d(gatewayInfo.getPos().getX(), gatewayInfo.getPos().getY(), gatewayInfo.getPos().getZ())) < MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE) {
            BlockPos gatewayPos = gatewayInfo.getPos();
            BlockPos entityExitPos = gatewayInfo.getEntityExitPos();
            float currentLine = -1F;

            // Prepare renderer
            GlStateManager.disableTexture();
            GlStateManager.enableBlend();

            // Draw lines
//            drawBoxSides(gatewayPos.getX() - 5, 255, gatewayPos.getZ() - 5, gatewayPos.getX() + 5 + 1, 0, gatewayPos.getZ() + 5 + 1, new Color(176, 93, 255, 50), 5, camera);
            drawBoxOutlined(gatewayPos.getX() + 5 + 1, 0, gatewayPos.getZ() + 5 + 1, gatewayPos.getX() - 5, 255, gatewayPos.getZ() - 5, new Color(128, 56, 201, 255), 5, camera);
            drawBoxOutlined(entityExitPos.getX(), entityExitPos.getY(), entityExitPos.getZ(), entityExitPos.getX() + 1, entityExitPos.getY() + 1, entityExitPos.getZ() + 1, new Color(0, 162, 232, 255), 5, camera);

            // Draw String
            drawString(new TranslatableText("gatewayer.entity_exit_position", gatewayInfo.getEntityExitPos().toShortString()).getString(), gatewayInfo.getEntityExitPos(), tickDelta, Formatting.BLUE.getColorValue(), 0F);
            drawString(new TranslatableText("gatewayer.gateway_position", gatewayInfo.getPos().toShortString()).getString(), gatewayInfo.getPos(), tickDelta, Formatting.GOLD.getColorValue(), currentLine ++);

            if (gatewayInfo.hasBlockEntity()) {
                EndGatewayBlockEntity gateway = gatewayInfo.getBlockEntity();
                BlockPos exitPortalPos = ((EndGatewayBlockEntityAccessor) gateway).getExitPortalPos();
                boolean exactTeleport = ((EndGatewayBlockEntityAccessor) gateway).getExactTeleport();
                int teleportCooldown = ((EndGatewayBlockEntityAccessor) gateway).getTeleportCooldown();
                if (exitPortalPos != null) {
                    drawString(new TranslatableText("gatewayer.exit_portal", exitPortalPos.toShortString()).getString(), gatewayInfo.getPos(), tickDelta, Formatting.DARK_GREEN.getColorValue(), currentLine ++);
                } else {
                    drawString(new TranslatableText("gatewayer.no_exit_portal").getString(), gatewayInfo.getPos(), tickDelta, Formatting.RED.getColorValue(), currentLine ++);
                }
                if (gateway.needsCooldownBeforeTeleporting()) {
                    drawString(new TranslatableText("gatewayer.cooling_down", teleportCooldown).getString(), gatewayInfo.getPos(), tickDelta, gateway.needsCooldownBeforeTeleporting() ? Formatting.RED.getColorValue() : Formatting.DARK_GREEN.getColorValue(), currentLine ++);
                } else {
                    drawString(new TranslatableText("gatewayer.ready_to_teleport").getString(), gatewayInfo.getPos(), tickDelta, gateway.needsCooldownBeforeTeleporting() ? Formatting.RED.getColorValue() : Formatting.DARK_GREEN.getColorValue(), currentLine++);
                }
                if (exactTeleport) {
                    drawString(new TranslatableText("gatewayer.exact_teleport").getString(), gatewayInfo.getPos(), tickDelta, Formatting.YELLOW.getColorValue(), currentLine ++);
                }
            } else {
                drawString(new TranslatableText("gatewayer.non_block_entity").getString(), gatewayInfo.getPos(), tickDelta, Formatting.RED.getColorValue(), currentLine ++);
            }
        }
    }

    public static void drawBoxSides(double startX, double startY, double startZ, double endX, double endY, double endZ, Color color, float lineWidth, Camera camera) {
        drawWall(startX, startY, startZ, startX, endY, endZ, color, lineWidth, camera);
        drawWall(endX, startY, startZ, endX, endY, endZ, color, lineWidth, camera);
        drawWall(startX, startY, startZ, endX, endY, startZ, color, lineWidth, camera);
        drawWall(startX, startY, endZ, endX, endY, endZ, color, lineWidth, camera);
    }

    public static void drawLine(double startX, double startY, double startZ, double endX, double endY, double endZ, Color color, float lineWidth, Camera camera) {
        double cameraX = camera.getPos().getX();
        double cameraY = camera.getPos().getY();
        double cameraZ = camera.getPos().getZ();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
        GlStateManager.lineWidth(lineWidth);

        bufferBuilder.vertex(startX - cameraX, startY - cameraY, startZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(endX - cameraX, endY - cameraY, endZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

        Tessellator.getInstance().draw();
        GlStateManager.lineWidth(lineWidth);
    }

    public static void drawWall(double startX, double startY, double startZ, double endX, double endY, double endZ, Color color, float lineWidth, Camera camera) {
        double cameraX = camera.getPos().getX();
        double cameraY = camera.getPos().getY();
        double cameraZ = camera.getPos().getZ();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(GL11.GL_QUAD_STRIP, VertexFormats.POSITION_COLOR);
        GlStateManager.lineWidth(lineWidth);

        bufferBuilder.vertex(startX - cameraX, startY - cameraY, startZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(endX - cameraX, startY - cameraY, endZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(startX - cameraX, endY - cameraY, startZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(endX - cameraX, endY - cameraY, endZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

        Tessellator.getInstance().draw();
        GlStateManager.lineWidth(lineWidth);

    }

    public static void drawBoxOutlined(double startX, double startY, double startZ, double endX, double endY, double endZ, Color color, float lineWidth, Camera camera) {
        double cameraX = camera.getPos().getX();
        double cameraY = camera.getPos().getY();
        double cameraZ = camera.getPos().getZ();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
        GlStateManager.lineWidth(lineWidth);

        bufferBuilder.vertex(startX - cameraX, startY - cameraY, startZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(startX - cameraX, startY - cameraY, startZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(endX - cameraX, startY - cameraY, startZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(endX - cameraX, startY - cameraY, endZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(startX - cameraX, startY - cameraY, endZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(startX - cameraX, startY - cameraY, startZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(startX - cameraX, endY - cameraY, startZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(endX - cameraX, endY - cameraY, startZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(endX - cameraX, endY - cameraY, endZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(startX - cameraX, endY - cameraY, endZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(startX - cameraX, endY - cameraY, startZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(startX - cameraX, endY - cameraY, endZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(startX - cameraX, startY - cameraY, endZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(endX - cameraX, startY - cameraY, endZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(endX - cameraX, endY - cameraY, endZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(endX - cameraX, endY - cameraY, startZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(endX - cameraX, startY - cameraY, startZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex(endX - cameraX, startY - cameraY, startZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

        Tessellator.getInstance().draw();
        GlStateManager.lineWidth(1.0f);
    }

    public static void drawString(String text, BlockPos pos, float tickDelta, int color, float line)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (camera.isReady() && client.getEntityRenderManager().gameOptions != null && client.player != null) {
            double x = (double)pos.getX() + 0.5D;
            double y = (double)pos.getY() + 0.5D;
            double z = (double)pos.getZ() + 0.5D;
            if (client.player.squaredDistanceTo(x, y, z) > MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE) {
                return;
            }
            double camX = camera.getPos().x;
            double camY = camera.getPos().y;
            double camZ = camera.getPos().z;
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float)(x - camX), (float)(y - camY), (float)(z - camZ));
            RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
            RenderSystem.multMatrix(new Matrix4f(camera.getRotation()));
            RenderSystem.scalef(FONT_SIZE, -FONT_SIZE, FONT_SIZE);
            RenderSystem.enableTexture();
            RenderSystem.disableDepthTest();  // visibleThroughObjects
            RenderSystem.depthMask(true);
            RenderSystem.scalef(-1.0F, 1.0F, 1.0F);
            RenderSystem.enableAlphaTest();

            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            float renderX = -client.textRenderer.getStringWidth(text) * 0.5F;
            float renderY = client.textRenderer.getStringBoundedHeight(text, Integer.MAX_VALUE) * (-0.5F + 1.25F * line);
            Matrix4f matrix4f = Rotation3.identity().getMatrix();
            client.textRenderer.draw(text, renderX, renderY, color, false, matrix4f, immediate, true, 0, 0xF000F0);
            immediate.draw();

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableDepthTest();
            RenderSystem.popMatrix();
        }
    }

}
