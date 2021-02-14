package io.github.ultimateboomer.resolutioncontrol.client.gui.screen;

import io.github.ultimateboomer.resolutioncontrol.ResolutionControlMod;
import io.github.ultimateboomer.resolutioncontrol.util.RCMathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;


@SuppressWarnings("FieldCanBeLocal")
public final class MainSettingsScreen extends SettingsScreen {
	private static final Identifier backgroundTexture = ResolutionControlMod.identifier("textures/gui/settings.png");

	private static final double[] scaleValues = {0.0, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 0.75, 1.0,
			1.25, 1.5, 2.0, 3.0, 4.0, 6.0, 8.0};

	private static final double redValue = 2.0;

	private static final Text increaseText = new LiteralText("+");
	private static final Text decreaseText = new LiteralText("-");
	private static final Text setText = new LiteralText("S");
	private static final Text cancelText = new LiteralText("C");
	
	private ButtonWidget increaseButton;
	private ButtonWidget decreaseButton;
	private ButtonWidget setButton;
	private ButtonWidget cancelButton;

	private TextFieldWidget entryTextField;

	private ButtonWidget upscaleAlgoButton;
	private ButtonWidget downscaleAlgoButton;

//	private ButtonWidget doneButton;

	private boolean manualEntry = false;
	
	public MainSettingsScreen(@Nullable Screen parent) {
		super(text("settings.main"), parent);
	}
	
	public MainSettingsScreen() {
		this(MinecraftClient.getInstance().currentScreen);
	}
	
	@Override
	protected void init() {
		super.init();
		mainSettingsButton.active = false;
		
		int buttonSize = 20;
		int buttonOffset = buttonSize / 2;
		int buttonY = centerY + 15 - buttonSize / 2;
		int textFieldSize = 40;
		
		decreaseButton = new ButtonWidget(
			centerX - 55 - buttonOffset - buttonSize / 2, buttonY,
			buttonSize, buttonSize,
			decreaseText,
			button -> changeScaleFactor(false));
		addButton(decreaseButton);
		
		increaseButton = new ButtonWidget(
			centerX - 55 + buttonOffset - buttonSize / 2, buttonY,
			buttonSize, buttonSize,
				increaseText,
			button -> changeScaleFactor(true)
		);
		addButton(increaseButton);

		setButton = new ButtonWidget(
				centerX - 55 - buttonOffset - buttonSize / 2, buttonY + buttonSize,
				buttonSize, buttonSize,
				setText,
				button -> {
					setManualEntry(!manualEntry, false);
				}
		);
		addButton(setButton);

		cancelButton = new ButtonWidget(
				centerX - 55 - buttonOffset + buttonSize / 2, buttonY + buttonSize,
				buttonSize, buttonSize,
				cancelText,
				button -> {
					setManualEntry(false, true);
				}
		);
		cancelButton.active = false;
		addButton(cancelButton);

		entryTextField = new TextFieldWidget(client.textRenderer,
				centerX - 55 - textFieldSize / 2, centerY - 36,
				textFieldSize, buttonSize, LiteralText.EMPTY);
		entryTextField.setVisible(false);
		addButton(entryTextField);

		upscaleAlgoButton = new ButtonWidget(
			centerX + 15, centerY - 28,
			60, buttonSize,
			mod.getUpscaleAlgorithm().getText(),
			button -> {
				mod.nextUpscaleAlgorithm();
				button.setMessage(mod.getUpscaleAlgorithm().getText());
			}
		);
		addButton(upscaleAlgoButton);

		downscaleAlgoButton = new ButtonWidget(
				centerX + 15, centerY + 8,
				60, buttonSize,
				mod.getDownscaleAlgorithm().getText(),
				button -> {
					mod.nextDownscaleAlgorithm();
					button.setMessage(mod.getDownscaleAlgorithm().getText());
				}
		);
		addButton(downscaleAlgoButton);
		
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
	public void render(MatrixStack matrices, int mouseX, int mouseY, float time) {
		super.render(matrices, mouseX, mouseY, time);

		drawCenteredString(matrices, String.format("\u00a7%s%s\u00a7rx",
				mod.getScaleFactor() > redValue ? "4" : "0", mod.getScaleFactor()),
				centerX - 55, centerY - 36, 0x000000);

		drawCenteredString(matrices, String.format("\u00a78%sx%s\u00a7r",
				ResolutionControlMod.getInstance().getCurrentWidth(),
				ResolutionControlMod.getInstance().getCurrentHeight()),
				centerX - 55, centerY - 24, 0x000000);

		drawCenteredString(matrices, "\u00a78" + text("settings.main.estimate",
				RCMathUtil.formatMetric(ResolutionControlMod.getInstance().getEstimatedMemory()) + "B")
				.getString() + "\u00a7r",
				centerX - 55, centerY - 12, 0x000000);

		drawLeftAlignedString(matrices,
				"\u00a78" + text("settings.main.upscale").getString(),
				centerX + 15, centerY - 40, 0x000000);
		drawLeftAlignedString(matrices,
				"\u00a78" + text("settings.main.downscale").getString(),
				centerX + 15, centerY - 5, 0x000000);


	}

	@Override
	public void tick() {
		if (manualEntry) {
			if (!this.getFocused().equals(entryTextField)) {
				this.focusOn(entryTextField);
			}

			if (!entryTextField.isFocused()) {
				entryTextField.setSelected(true);
			}

			if (!entryTextField.active) {
				entryTextField.active = true;
			}
		}

		entryTextField.tick();
		super.tick();
	}
	
	private void changeScaleFactor(boolean add) {
		double currentScale = mod.getScaleFactor();
		int nextIndex = ArrayUtils.indexOf(scaleValues, currentScale);
		if (nextIndex == -1) {
			for (int i = -1; i <= scaleValues.length; ++i) {
				double scale1 = i == -1 ? 0.0 : scaleValues[i];
				double scale2 = i == scaleValues.length ? Double.POSITIVE_INFINITY : scaleValues[i + 1];

				if (currentScale > scale1 && currentScale < scale2) {
					nextIndex = i + (add ? 1 : 0);
					break;
				}
			}
		} else {
			nextIndex += add ? 1 : -1;
		}

		mod.setScaleFactor(scaleValues[nextIndex]);

		updateButtons();
	}
	
	private void updateButtons() {
		increaseButton.active = mod.getScaleFactor() < scaleValues[scaleValues.length - 1];
		decreaseButton.active = mod.getScaleFactor() > scaleValues[0];
	}

	public void setManualEntry(boolean manualEntry, boolean cancel) {
		this.manualEntry = manualEntry;
		if (manualEntry) {
			entryTextField.setText(String.valueOf(mod.getScaleFactor()));
			entryTextField.setVisible(true);
			entryTextField.setSelectionStart(0);
			entryTextField.setSelectionEnd(entryTextField.getText().length());
			entryTextField.active = true;
			cancelButton.active = true;
			increaseButton.active = false;
			decreaseButton.active = false;
			this.focusOn(entryTextField);
		} else {
			if (!cancel) {
				String text = entryTextField.getText();
				if (NumberUtils.isParsable(text)) {
					double value = Double.parseDouble(text);
					mod.setScaleFactor(value);
					updateButtons();
				}
			}

			entryTextField.setVisible(false);
			setButton.setMessage(setText);
			cancelButton.active = false;
			increaseButton.active = true;
			decreaseButton.active = true;
		}
	}
}
