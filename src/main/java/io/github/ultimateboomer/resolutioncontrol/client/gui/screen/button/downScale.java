package io.github.ultimateboomer.resolutioncontrol.client.gui.screen.button;

import io.github.ultimateboomer.resolutioncontrol.ResolutionControlMod;
import io.github.ultimateboomer.resolutioncontrol.util.Config;
import io.github.ultimateboomer.resolutioncontrol.util.ScalingAlgorithm;
import net.minecraft.client.gui.components.Button;

public final class downScale {
  private downScale() {
    throw new IllegalStateException("Utility Class");
  }

  public static Button downScaleButton(int centerX, int centerY, int buttonSize) {
    return new Button.Builder(
            Config.getDownscaleAlgorithm().getText(),
            button -> {
              nextDownscaleAlgorithm();
              button.setMessage(Config.getDownscaleAlgorithm().getText());
            })
        .bounds(centerX + 15, centerY + 8, 60, buttonSize)
        .build();
  }

  private static void nextDownscaleAlgorithm() {
    ScalingAlgorithm currentAlgorithm = Config.getDownscaleAlgorithm();
    if (currentAlgorithm.equals(ScalingAlgorithm.NEAREST)) {
      setDownscaleAlgorithm(ScalingAlgorithm.LINEAR);
    } else {
      setDownscaleAlgorithm(ScalingAlgorithm.NEAREST);
    }
  }

  private static void setDownscaleAlgorithm(ScalingAlgorithm algorithm) {
    if (algorithm == Config.getDownscaleAlgorithm()) return;

    Config.setDownscaleAlgorithm(algorithm);

    ResolutionControlMod.getInstance().updateFramebufferSize();
  }
}
