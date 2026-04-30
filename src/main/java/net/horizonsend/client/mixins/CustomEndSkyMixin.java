package net.horizonsend.client.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.dimension.DimensionTypes;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.horizonsend.client.SkyBoxSystemKt.currentSkybox;

@Mixin(WorldRenderer.class)
public class CustomEndSkyMixin {

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void onRenderSky(
            net.minecraft.client.render.Camera camera,
            float tickDelta,
            Matrix4f projectionMatrix,
            CallbackInfo ci)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        if (!client.world.getDimensionEntry().matchesKey(DimensionTypes.THE_END)) return;

        ci.cancel();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, currentSkybox().getIdentifier());

        MatrixStack matrices = new MatrixStack();
        Tessellator tessellator = Tessellator.getInstance();
        float[] points = new float[8];

        for (int i = 0; i < 6; i++) {
            matrices.push();
            if (i == 0)
                points = new float[]{0.33333334F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, 0.33333334F, 0.5F};
            if (i == 1) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
                points = new float[]{0.6666667F, 0.5F, 0.6666667F, 1.0F, 1.0F, 1.0F, 1.0F, 0.5F};
            }
            if (i == 2) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
                points = new float[]{0.33333334F, 1.0F, 0.33333334F, 0.5F, 0.0F, 0.5F, 0.0F, 1.0F};
            }
            if (i == 3) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0f));
                points = new float[]{0.33333334F, 0.5F, 0.6666667F, 0.5F, 0.6666667F, 0.0F, 0.33333334F, 0.0F};
            }
            if (i == 4) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
                points = new float[]{0.6666667F, 0.5F, 1.0F, 0.5F, 1.0F, 0.0F, 0.6666667F, 0.0F};
            }
            if (i == 5) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-90.0F));
                points = new float[]{0.6666667F, 0.5F, 0.33333334F, 0.5F, 0.33333334F, 1.0F, 0.6666667F, 1.0F};
            }

            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).texture(points[0], points[1]).color(255, 255, 255, 255);
            bufferBuilder.vertex(matrix4f, -100.0F, -100.0F,  100.0F).texture(points[2], points[3]).color(255, 255, 255, 255);
            bufferBuilder.vertex(matrix4f,  100.0F, -100.0F,  100.0F).texture(points[4], points[5]).color(255, 255, 255, 255);
            bufferBuilder.vertex(matrix4f,  100.0F, -100.0F, -100.0F).texture(points[6], points[7]).color(255, 255, 255, 255);
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
            matrices.pop();
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}