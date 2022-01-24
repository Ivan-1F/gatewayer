package me.ivan.gatewayer.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.ivan.gatewayer.Gatewayer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    @Inject(method = "render", at = @At(value = "RETURN"))
    private void renderGatewayer(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci)
    {
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.method_34425(matrices.peek().getModel());
        RenderSystem.applyModelViewMatrix();
        Gatewayer.getInstance().renderTexts(matrixStack, tickDelta);
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
    }



}
