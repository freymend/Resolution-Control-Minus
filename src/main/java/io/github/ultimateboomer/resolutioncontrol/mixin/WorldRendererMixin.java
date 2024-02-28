package io.github.ultimateboomer.resolutioncontrol.mixin;

import io.github.ultimateboomer.resolutioncontrol.ResolutionControlMod;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

  /**
   * If the player has changed the scaleFactor of the mod, entities will not be rendered in the
   * correct position. We need to resize the entityOutlinesFramebuffer before the player enters a
   * world to prevent the player from seeing entities being rendered in the wrong position. This
   * function happens to be called one time on client start up, and happens after the framebuffers
   * have been created, so we can resize the entityOutlinesFramebuffer here.
   */
  @Inject(at = @At("RETURN"), method = "loadEntityOutlinePostProcessor")
  private void onLoadEntityOutlineShader(CallbackInfo ci) {
    ResolutionControlMod.getInstance().resizeEntityOutlinesFramebuffer();
  }
}
