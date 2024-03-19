package io.github.ultimateboomer.resolutioncontrol.client.gui.screen.button;

import io.github.ultimateboomer.resolutioncontrol.ResolutionControlMod;
import io.github.ultimateboomer.resolutioncontrol.util.Config;
import io.github.ultimateboomer.resolutioncontrol.util.ScalingAlgorithm;
import net.minecraft.client.gui.components.Button;

public final class upScale {
  private upScale() {
    throw new IllegalStateException("Utility Class");
  }

  public static Button upscaleAlgoButton(int centerX, int centerY, int buttonSize) {
    return new Button.Builder(
            Config.getUpscaleAlgorithm().getText(),
            button -> {
              nextUpscaleAlgorithm();
              button.setMessage(Config.getUpscaleAlgorithm().getText());
            })
        .bounds(centerX + 15, centerY - 28, 60, buttonSize)
        .build();
  }

  private static void nextUpscaleAlgorithm() {
    ScalingAlgorithm currentAlgorithm = Config.getUpscaleAlgorithm();
    if (currentAlgorithm.equals(ScalingAlgorithm.NEAREST)) {
      setUpscaleAlgorithm(ScalingAlgorithm.LINEAR);
    } else {
      setUpscaleAlgorithm(ScalingAlgorithm.NEAREST);
    }
  }

  private static void setUpscaleAlgorithm(ScalingAlgorithm algorithm) {
    if (algorithm == Config.getUpscaleAlgorithm()) return;

    Config.setUpscaleAlgorithm(algorithm);

    ResolutionControlMod.getInstance().updateFramebufferSize();
  }
}
