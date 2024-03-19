package io.github.ultimateboomer.resolutioncontrol.mixin;

import io.github.ultimateboomer.resolutioncontrol.ResolutionControlMod;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
  @Inject(at = @At("HEAD"), method = "renderLevel")
  private void onRenderWorldBegin(CallbackInfo callbackInfo) {
    ResolutionControlMod.getInstance().setShouldScale(true);
  }

  @Inject(at = @At("RETURN"), method = "renderLevel")
  private void onRenderWorldEnd(CallbackInfo callbackInfo) {
    ResolutionControlMod.getInstance().setShouldScale(false);
  }
}
