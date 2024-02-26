package io.github.ultimateboomer.resolutioncontrol.mixin;

import io.github.ultimateboomer.resolutioncontrol.ResolutionControlMod;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Window.class)
public abstract class WindowMixin {
  /**
   * This method modifies the width of the FrameBuffer to match the current scale factor of the mod, which causes
   * the game to render at the resolution of the scaled window. But this only resizes the Client Window FrameBuffer,
   * not the other FrameBuffers used in rendering the game.
   */
  @Inject(at = @At("RETURN"), method = "getFramebufferWidth", cancellable = true)
  private void getFramebufferWidth(CallbackInfoReturnable<Integer> ci) {
    ci.setReturnValue(scale(ci.getReturnValueI()));
  }

  /**
   * This method modifies the width of the FrameBuffer to match the current scale factor of the mod, which causes
   * the game to render at the resolution of the scaled window. But this only resizes the Client Window FrameBuffer,
   * not the other FrameBuffers used in rendering the game.
   */
  @Inject(at = @At("RETURN"), method = "getFramebufferHeight", cancellable = true)
  private void getFramebufferHeight(CallbackInfoReturnable<Integer> ci) {
    ci.setReturnValue(scale(ci.getReturnValueI()));
  }

  @Unique
  private int scale(int value) {
    double scaleFactor = ResolutionControlMod.getInstance().getCurrentScaleFactor();
    return Math.max(MathHelper.ceil((double) value * scaleFactor), 1);
  }

  /**
   * This method is called when the client window is resized. We need to resize the FrameBuffers to
   * match the new window size.
   */
  @Inject(at = @At("RETURN"), method = "onFramebufferSizeChanged")
  private void onFramebufferSizeChanged(CallbackInfo ci) {
    ResolutionControlMod.getInstance().onResolutionChanged();
  }
}
