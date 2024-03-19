package io.github.ultimateboomer.resolutioncontrol.client.gui.screen;

import io.github.ultimateboomer.resolutioncontrol.ResolutionControlMod;
import io.github.ultimateboomer.resolutioncontrol.client.gui.screen.button.downScale;
import io.github.ultimateboomer.resolutioncontrol.client.gui.screen.button.upScale;
import io.github.ultimateboomer.resolutioncontrol.util.Config;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@SuppressWarnings("FieldCanBeLocal")
public final class MainSettingsScreen extends SettingsScreen {
  private static final List<Float> scaleValues =
      List.of(
          0.0f, 0.01f, 0.025f, 0.05f, 0.1f, 0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f, 4.0f,
          6.0f, 8.0f);

  private static final double redValue = 2.0;

  private static final Component increaseText = Component.literal("+");
  private static final Component decreaseText = Component.literal("-");
  private static final Component setText = Component.literal("S");
  private static final Component resetText = Component.literal("R");
  private static final Component cancelText = Component.literal("C");

  private Button increaseButton;
  private Button decreaseButton;
  private Button setButton;
  private Button cancelOrResetButton;

  private EditBox entryTextField;

  private boolean manualEntry = false;

  public MainSettingsScreen(@Nullable Screen parent) {
    super(text("settings.main"), parent);
  }

  @Override
  protected void init() {
    super.init();

    int buttonSize = 20;
    int buttonOffset = buttonSize / 2;
    int buttonY = centerY + 15 - buttonSize / 2;
    int textFieldSize = 40;

    decreaseButton =
        new Button.Builder(decreaseText, button -> changeScaleFactor(false))
            .bounds(
                centerX - 55 - buttonOffset - buttonSize / 2, buttonY, buttonSize, buttonSize)
            .build();
    super.addRenderableWidget(decreaseButton);

    increaseButton =
        new Button.Builder(increaseText, button -> changeScaleFactor(true))
            .bounds(
                centerX - 55 + buttonOffset - buttonSize / 2, buttonY, buttonSize, buttonSize)
            .build();
    super.addRenderableWidget(increaseButton);

    setButton =
        new Button.Builder(setText, button -> setManualEntry(!manualEntry, false))
            .bounds(
                centerX - 55 - buttonOffset - buttonSize / 2,
                buttonY + buttonSize,
                buttonSize,
                buttonSize)
            .build();
    super.addRenderableWidget(setButton);

    cancelOrResetButton =
        new Button.Builder(
                resetText,
                button -> {
                  if (manualEntry) {
                    setManualEntry(false, true);
                  } else {
                    mod.setScaleFactor(1.0f);
                    updateButtons();
                  }
                })
            .bounds(
                centerX - 55 - buttonOffset + buttonSize / 2,
                buttonY + buttonSize,
                buttonSize,
                buttonSize)
            .build();
    super.addRenderableWidget(cancelOrResetButton);

    entryTextField =
        new EditBox(
            font,
            centerX - 55 - textFieldSize / 2,
            centerY - 36,
            textFieldSize,
            buttonSize,
            Component.empty());
    entryTextField.setVisible(false);
    super.addRenderableWidget(entryTextField);

    var upscaleAlgoButton = upScale.upscaleAlgoButton(centerX, centerY, buttonSize);
    super.addRenderableWidget(upscaleAlgoButton);

    var downscaleAlgoButton = downScale.downScaleButton(centerX, centerY, buttonSize);
    super.addRenderableWidget(downscaleAlgoButton);

    updateButtons();
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (this.manualEntry) {
      if (keyCode == GLFW.GLFW_KEY_ENTER) {
        this.setManualEntry(false, false);
        return true;
      } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
        this.setManualEntry(false, true);
        return true;
      } else {
        return super.keyPressed(keyCode, scanCode, modifiers);
      }
    } else {
      return super.keyPressed(keyCode, scanCode, modifiers);
    }
  }

  @Override
  public void render(GuiGraphics context, int mouseX, int mouseY, float time) {
    super.render(context, mouseX, mouseY, time);

    if (!this.manualEntry) {
      drawCenteredString(
          context,
          String.format(
              "\u00a7%s%s\u00a7rx",
              Config.getScaleFactor() > redValue ? "4" : "0", Config.getScaleFactor()),
          centerX - 55,
          centerY - 36,
          0x000000);

      drawCenteredString(
          context,
          String.format(
              "\u00a78%sx%s\u00a7r",
              ResolutionControlMod.getInstance().getCurrentWidth(),
              ResolutionControlMod.getInstance().getCurrentHeight()),
          centerX - 55,
          centerY - 24,
          0x000000);

      drawCenteredString(
          context,
          "\u00a78"
              + text(
                      "settings.main.estimate",
                      ResolutionControlMod.getInstance().getEstimatedMemory())
                  .getString()
              + "\u00a7r",
          centerX - 55,
          centerY - 12,
          0x000000);
    }

    drawLeftAlignedString(
        context,
        "\u00a78" + text("settings.main.upscale").getString(),
        centerX + 15,
        centerY - 40,
        0x000000);
    drawLeftAlignedString(
        context,
        "\u00a78" + text("settings.main.downscale").getString(),
        centerX + 15,
        centerY - 5,
        0x000000);
  }

  @Override
  public void tick() {
    if (manualEntry) {
      if (!super.getFocused().equals(entryTextField)) {
        super.magicalSpecialHackyFocus(entryTextField);
      }

      if (!entryTextField.active) {
        entryTextField.active = true;
      }
    }
    super.tick();
  }

  private void changeScaleFactor(boolean add) {
    float currentScale = Config.getScaleFactor();
    int nextIndex = scaleValues.indexOf(currentScale);
    if (nextIndex != -1) {
      nextIndex += add ? 1 : -1;
    }

    mod.setScaleFactor(scaleValues.get(nextIndex));

    updateButtons();
  }

  private void updateButtons() {
    increaseButton.active = Config.getScaleFactor() < scaleValues.get(scaleValues.size() - 1);
    decreaseButton.active = Config.getScaleFactor() > scaleValues.get(0);
  }

  public void setManualEntry(boolean manualEntry, boolean cancel) {
    this.manualEntry = manualEntry;
    if (manualEntry) {
      entryTextField.setValue(String.valueOf(Config.getScaleFactor()));
      entryTextField.setVisible(true);
      entryTextField.setCursorPosition(0);
      entryTextField.setHighlightPos(entryTextField.getValue().length());
      entryTextField.active = true;
      cancelOrResetButton.setMessage(cancelText);
      increaseButton.active = false;
      decreaseButton.active = false;
      super.magicalSpecialHackyFocus(entryTextField);
    } else {
      if (!cancel) {
        String text = entryTextField.getValue();
        try {
          float value = Math.abs(Float.parseFloat(text));
          mod.setScaleFactor(value);
        } catch (NumberFormatException e) {
          /* Ignore invalid format */
        }
      }

      entryTextField.setVisible(false);
      setButton.setMessage(setText);
      cancelOrResetButton.setMessage(resetText);
      increaseButton.active = true;
      decreaseButton.active = true;

      updateButtons();
    }
  }
}
