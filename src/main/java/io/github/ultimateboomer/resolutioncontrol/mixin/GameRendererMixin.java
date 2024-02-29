package io.github.ultimateboomer.resolutioncontrol.mixin;

import io.github.ultimateboomer.resolutioncontrol.ResolutionControlMod;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.ResourceReloader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
  @Inject(at = @At("HEAD"), method = "renderWorld")
  private void onRenderWorldBegin(CallbackInfo callbackInfo) {
    ResolutionControlMod.getInstance().setShouldScale(true);
  }

  @Inject(at = @At("RETURN"), method = "renderWorld")
  private void onRenderWorldEnd(CallbackInfo callbackInfo) {
    ResolutionControlMod.getInstance().setShouldScale(false);
  }

  /**
   * This mixin is used to initialize the framebuffer after the game has started and when there is a
   * valid window object. I'm too lazy to find a better way to do this. So we're just going to use
   * this function because it's called after the window is created.
   */
  @Inject(at = @At("RETURN"), method = "createProgramReloader")
  private void onCreateProgramReloader(CallbackInfoReturnable<ResourceReloader> cir) {
    ResolutionControlMod.getInstance().initFramebuffer();
  }
}
