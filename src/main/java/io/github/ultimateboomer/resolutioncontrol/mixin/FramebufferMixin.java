package io.github.ultimateboomer.resolutioncontrol.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.ultimateboomer.resolutioncontrol.util.Config;
import java.nio.IntBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL45;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderTarget.class)
public abstract class FramebufferMixin {
  @Unique private boolean isMipmapped;
  @Unique private float scaleMultiplier;

  @Shadow
  public abstract int getColorTextureId();

  @Inject(method = "createBuffers", at = @At("HEAD"))
  private void onInitFbo(int width, int height, boolean getError, CallbackInfo ci) {
    scaleMultiplier = (float) width / Minecraft.getInstance().getWindow().getWidth();
    isMipmapped = Config.getMipmapHighRes() && scaleMultiplier > 2.0f;
  }

  @Redirect(
      method = "*",
      at =
          @At(
              value = "INVOKE",
              target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texParameter(III)V"))
  private void onSetTexFilter(int target, int pname, int param) {
    if (pname == GL11.GL_TEXTURE_MIN_FILTER) {
      GlStateManager._texParameter(
          target,
          pname,
          Config.getUpscaleAlgorithm().getId(isMipmapped));
    } else if (pname == GL11.GL_TEXTURE_MAG_FILTER) {
      GlStateManager._texParameter(
          target, pname, Config.getDownscaleAlgorithm().getId(false));
    } else if (pname == GL11.GL_TEXTURE_WRAP_S || pname == GL11.GL_TEXTURE_WRAP_T) {
      // Fix linear scaling creating black borders
      GlStateManager._texParameter(target, pname, GL12.GL_CLAMP_TO_EDGE);
    } else {
      GlStateManager._texParameter(target, pname, param);
    }
  }

  @Redirect(
      method = "createBuffers",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V"))
  private void onTexImage(
      int target,
      int level,
      int internalFormat,
      int width,
      int height,
      int border,
      int format,
      int type,
      IntBuffer pixels) {
    if (isMipmapped) {
      int mipmapLevel = Mth.ceil(Math.log(scaleMultiplier) / Math.log(2));
      for (int i = 0; i < mipmapLevel; i++) {
        GlStateManager._texImage2D(
            target, i, internalFormat, width << i, height << i, border, format, type, pixels);
      }
    } else {
      GlStateManager._texImage2D(
          target, 0, internalFormat, width, height, border, format, type, pixels);
    }
  }

  @Inject(method = "_blitToScreen", at = @At("HEAD"))
  private void onDraw(int width, int height, boolean bl, CallbackInfo ci) {
    if (isMipmapped) {
      GlStateManager._bindTexture(this.getColorTextureId());
      GL45.glGenerateMipmap(GL11.GL_TEXTURE_2D);
    }
  }
}
