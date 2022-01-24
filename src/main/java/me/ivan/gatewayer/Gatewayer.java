package me.ivan.gatewayer;

import com.mojang.blaze3d.systems.RenderSystem;
import me.ivan.gatewayer.utils.GatewayInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndGatewayBlock;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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

    public void renderTexts(MatrixStack stack, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();
        if (gatewayInfo.isEnabled() && camera.getPos().squaredDistanceTo(new Vec3d(gatewayInfo.getPos().getX(), gatewayInfo.getPos().getY(), gatewayInfo.getPos().getZ())) < MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE) {
            float currentLine = -1F;

            drawString(gatewayInfo.getEntityExitPos(), tickDelta, 0F, new String[]{new TranslatableText("gatewayer.entity_exit_position", gatewayInfo.getEntityExitPos().toShortString()).getString()}, new int[]{Formatting.BLUE.getColorValue()});
            drawString(gatewayInfo.getPos(), tickDelta, currentLine++, new String[]{new TranslatableText("gatewayer.gateway_position", gatewayInfo.getPos().toShortString()).getString()}, new int[]{Formatting.GOLD.getColorValue()});

            if (gatewayInfo.hasBlockEntity()) {
                EndGatewayBlockEntity gateway = gatewayInfo.getBlockEntity();
                NbtCompound tag = new NbtCompound();
                gateway.writeNbt(tag);
                if (tag.contains("ExitPortal")) {
                    drawString(gatewayInfo.getPos(), tickDelta, currentLine++, new String[]{new TranslatableText("gatewayer.exit_portal", tag.get("ExitPortal").toString()).getString().replace("{", "").replace("}", "")}, new int[]{Formatting.DARK_GREEN.getColorValue()});
                } else {
                    drawString(gatewayInfo.getPos(), tickDelta, currentLine++, new String[]{new TranslatableText("gatewayer.no_exit_portal").getString()}, new int[]{Formatting.RED.getColorValue()});
                }
                drawString(gatewayInfo.getPos(), tickDelta, currentLine++, new String[]{new TranslatableText(gateway.needsCooldownBeforeTeleporting() ? "gatewayer.cooling_down" : "gatewayer.ready_to_teleport").getString()}, new int[]{gateway.needsCooldownBeforeTeleporting() ? Formatting.RED.getColorValue() : Formatting.DARK_GREEN.getColorValue()});

                if (tag.contains("ExactTeleport") && tag.get("ExactTeleport").toString().equals("1b")) {
                    drawString(gatewayInfo.getPos(), tickDelta, currentLine++, new String[]{new TranslatableText("gatewayer.exact_teleport").getString()}, new int[]{Formatting.YELLOW.getColorValue()});
                }
            } else {
                drawString(gatewayInfo.getPos(), tickDelta, currentLine++, new String[]{new TranslatableText("gatewayer.non_block_entity").getString()}, new int[]{Formatting.RED.getColorValue()});
            }
        }
    }

    public void renderLines(MatrixStack stack, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();
        if (gatewayInfo.isEnabled() && camera.getPos().squaredDistanceTo(new Vec3d(gatewayInfo.getPos().getX(), gatewayInfo.getPos().getY(), gatewayInfo.getPos().getZ())) < MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE) {
            BlockPos gatewayPos = gatewayInfo.getPos();
            BlockPos entityExitPos = gatewayInfo.getEntityExitPos();
            float currentLine = -1F;

            // Prepare renderer
            RenderSystem.lineWidth(2.0f);

            RenderSystem.disableTexture();
            RenderSystem.disableBlend();


            // Draw lines
//            drawBoxSides(gatewayPos.getX() - 5, 255, gatewayPos.getZ() - 5, gatewayPos.getX() + 5 + 1, 0, gatewayPos.getZ() + 5 + 1, new Color(176, 93, 255, 50), 5, camera);
            drawBox(stack, gatewayPos.getX() + 5 + 1, -64, gatewayPos.getZ() + 5 + 1, gatewayPos.getX() - 5, 319, gatewayPos.getZ() - 5, 0.5F, 0.2F, 0.78F, 1, camera);
//            drawBoxOutlined(gatewayPos.getX() + 5 + 1, 0, gatewayPos.getZ() + 5 + 1, gatewayPos.getX() - 5, 255, gatewayPos.getZ() - 5, new Color(128, 56, 201, 255), 5, camera);
            drawBox(stack, entityExitPos.getX(), entityExitPos.getY(), entityExitPos.getZ(), entityExitPos.getX() + 1, entityExitPos.getY() + 1, entityExitPos.getZ() + 1, 0F, 0.63F, 0.9F, 1, camera);
//            drawBoxOutlined(entityExitPos.getX(), entityExitPos.getY(), entityExitPos.getZ(), entityExitPos.getX() + 1, entityExitPos.getY() + 1, entityExitPos.getZ() + 1, new Color(0, 162, 232, 255), 5, camera);

        }
    }
    public static void drawBox(MatrixStack stack, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float red, float green, float blue, float alpha, Camera camera) {
        stack.push();
        stack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
        final Matrix4f model = stack.peek().getPositionMatrix();

        final Tessellator tess = Tessellator.getInstance();
        final BufferBuilder buffer = tess.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(5);
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        buffer.vertex(model, (float) minX, (float) minY, (float) minZ).color(red, green, blue, alpha)
                .next();
        buffer.vertex(model, (float) maxX, (float) minY, (float) minZ).color(red, green, blue, alpha)
                .next();
        buffer.vertex(model, (float) maxX, (float) minY, (float) maxZ).color(red, green, blue, alpha)
                .next();
        buffer.vertex(model, (float) minX, (float) minY, (float) maxZ).color(red, green, blue, alpha)
                .next();
        buffer.vertex(model, (float) minX, (float) minY, (float) minZ).color(red, green, blue, alpha)
                .next();

        buffer.vertex(model, (float) minX, (float) maxY, (float) minZ).color(red, green, blue, alpha)
                .next();

        buffer.vertex(model, (float) minX, (float) maxY, (float) minZ).color(red, green, blue, alpha)
                .next();
        buffer.vertex(model, (float) maxX, (float) maxY, (float) minZ).color(red, green, blue, alpha)
                .next();
        buffer.vertex(model, (float) maxX, (float) maxY, (float) maxZ).color(red, green, blue, alpha)
                .next();
        buffer.vertex(model, (float) minX, (float) maxY, (float) maxZ).color(red, green, blue, alpha)
                .next();
        buffer.vertex(model, (float) minX, (float) maxY, (float) minZ).color(red, green, blue, alpha)
                .next();

        buffer.vertex(model, (float) minX, (float) maxY, (float) maxZ).color(red, green, blue, alpha)
                .next();
        buffer.vertex(model, (float) minX, (float) minY, (float) maxZ).color(red, green, blue, alpha)
                .next();

        buffer.vertex(model, (float) maxX, (float) minY, (float) maxZ).color(red, green, blue, alpha)
                .next();
        buffer.vertex(model, (float) maxX, (float) maxY, (float) maxZ).color(red, green, blue, alpha)
                .next();

        buffer.vertex(model, (float) maxX, (float) maxY, (float) minZ).color(red, green, blue, alpha)
                .next();
        buffer.vertex(model, (float) maxX, (float) minY, (float) minZ).color(red, green, blue, alpha)
                .next();
        tess.draw();
        stack.translate(camera.getPos().x, camera.getPos().y, camera.getPos().z);
    }

//    public static void drawBoxSides(double startX, double startY, double startZ, double endX, double endY, double endZ, Color color, float lineWidth, Camera camera) {
//        drawWall(startX, startY, startZ, startX, endY, endZ, color, lineWidth, camera);
//        drawWall(endX, startY, startZ, endX, endY, endZ, color, lineWidth, camera);
//        drawWall(startX, startY, startZ, endX, endY, startZ, color, lineWidth, camera);
//        drawWall(startX, startY, endZ, endX, endY, endZ, color, lineWidth, camera);
//    }

//    public static void drawLine(double startX, double startY, double startZ, double endX, double endY, double endZ, Color color, float lineWidth, Camera camera) {
//        double cameraX = camera.getPos().getX();
//        double cameraY = camera.getPos().getY();
//        double cameraZ = camera.getPos().getZ();
//
//        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
//        bufferBuilder.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
//        GlStateManager.lineWidth(lineWidth);
//
//        bufferBuilder.vertex(startX - cameraX, startY - cameraY, startZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
//        bufferBuilder.vertex(endX - cameraX, endY - cameraY, endZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
//
//        Tessellator.getInstance().draw();
//        GlStateManager.lineWidth(lineWidth);
//    }

//    public static void drawWall(double startX, double startY, double startZ, double endX, double endY, double endZ, Color color, float lineWidth, Camera camera) {
//        double cameraX = camera.getPos().getX();
//        double cameraY = camera.getPos().getY();
//        double cameraZ = camera.getPos().getZ();
//
//        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
//        bufferBuilder.begin(GL11.GL_QUAD_STRIP, VertexFormats.POSITION_COLOR);
//        GlStateManager.lineWidth(lineWidth);
//
//        bufferBuilder.vertex(startX - cameraX, startY - cameraY, startZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
//        bufferBuilder.vertex(endX - cameraX, startY - cameraY, endZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
//        bufferBuilder.vertex(startX - cameraX, endY - cameraY, startZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
//        bufferBuilder.vertex(endX - cameraX, endY - cameraY, endZ - cameraZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
//
//        Tessellator.getInstance().draw();
//        GlStateManager.lineWidth(lineWidth);
//
//    }

    public static void drawString(BlockPos pos, float tickDelta, float line, String[] texts, int[] colors) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (camera.isReady() && client.getEntityRenderDispatcher().gameOptions != null && client.player != null) {
            double x = (double) pos.getX() + 0.5D;
            double y = (double) pos.getY() + 0.5D;
            double z = (double) pos.getZ() + 0.5D;
            if (client.player.squaredDistanceTo(x, y, z) > MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE) {
                return;
            }
            double camX = camera.getPos().x;
            double camY = camera.getPos().y;
            double camZ = camera.getPos().z;
            MatrixStack matrixStack = RenderSystem.getModelViewStack();
            matrixStack.push();
            matrixStack.translate((float) (x - camX), (float) (y - camY), (float) (z - camZ));
            matrixStack.multiplyPositionMatrix(new Matrix4f(camera.getRotation()));
            matrixStack.scale(FONT_SIZE, -FONT_SIZE, FONT_SIZE);
            RenderSystem.enableTexture();
            RenderSystem.disableDepthTest();  // visibleThroughObjects
            RenderSystem.depthMask(true);
            matrixStack.scale(-1.0F, 1.0F, 1.0F);
            RenderSystem.applyModelViewMatrix();

            float totalWidth = 0.0F;
            for (String text : texts) {
                totalWidth += client.textRenderer.getWidth(text);
            }

            float writtenWidth = 0.0F;
            for (int i = 0; i < texts.length; i++) {
                VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                float renderX = -totalWidth * 0.5F + writtenWidth;
                float renderY = client.textRenderer.getWrappedLinesHeight(texts[i], Integer.MAX_VALUE) * (-0.5F + 1.25F * line);
                Matrix4f matrix4f = AffineTransformation.identity().getMatrix();
                client.textRenderer.draw(texts[i], renderX, renderY, colors[i], false, matrix4f, immediate, true, 0, 0xF000F0);
                immediate.draw();

                writtenWidth += client.textRenderer.getWidth(texts[i]);
            }

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableDepthTest();
            matrixStack.pop();
        }
    }
}
