package io.github.ultimateboomer.resolutioncontrol.mixin;

import com.mojang.blaze3d.platform.Window;
import io.github.ultimateboomer.resolutioncontrol.ResolutionControlMod;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Window.class)
public abstract class WindowMixin {
  /**
   * This method modifies the width of the FrameBuffer to match the current scale factor of the mod,
   * which causes the game to render at the resolution of the scaled window. But this only resizes
   * the Client Window FrameBuffer, not the other FrameBuffers used in rendering the game. The
   * rendering of the GUI isn't affected.
   */
  @Inject(at = @At("RETURN"), method = "getWidth", cancellable = true)
  private void getFramebufferWidth(CallbackInfoReturnable<Integer> ci) {
    ci.setReturnValue(scale(ci.getReturnValueI()));
  }

  /**
   * This method modifies the width of the FrameBuffer to match the current scale factor of the mod,
   * which causes the game to render at the resolution of the scaled window. But this only resizes
   * the Client Window FrameBuffer, not the other FrameBuffers used in rendering the game. The
   * rendering of the GUI isn't affected.
   */
  @Inject(at = @At("RETURN"), method = "getHeight", cancellable = true)
  private void getFramebufferHeight(CallbackInfoReturnable<Integer> ci) {
    ci.setReturnValue(scale(ci.getReturnValueI()));
  }

  /**
   * This method scales the value by the current scale factor of the mod. So if the scale factor is
   * 2 and the game is rendered at 1920x1080, then the resolution will be doubled to 3840x2560. The
   * value is rounded up to the nearest integer, and the minimum value is 1.
   */
  @Unique
  private int scale(int value) {
    double scaleFactor = ResolutionControlMod.getInstance().getCurrentScaleFactor();
    int roundedScaleValue = Mth.ceil((double) value * scaleFactor);
    return Math.max(roundedScaleValue, 1);
  }

  /**
   * This method is called when the client window is resized. The game is rendered at the resolution
   * of the scaled window, so we need to update the FrameBuffers to match the new window size.
   */
  @Inject(at = @At("RETURN"), method = "onFramebufferResize")
  private void onFramebufferSizeChanged(CallbackInfo ci) {
    ResolutionControlMod.getInstance().updateFramebufferSize();
  }
}
