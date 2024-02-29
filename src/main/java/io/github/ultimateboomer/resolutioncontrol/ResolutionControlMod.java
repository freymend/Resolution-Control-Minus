package io.github.ultimateboomer.resolutioncontrol;

import io.github.ultimateboomer.resolutioncontrol.client.gui.screen.MainSettingsScreen;
import io.github.ultimateboomer.resolutioncontrol.client.gui.screen.SettingsScreen;
import io.github.ultimateboomer.resolutioncontrol.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.WindowFramebuffer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class ResolutionControlMod implements ModInitializer {
  public static final String MOD_ID = "resolutioncontrol";
  public static final String MOD_NAME = "ResolutionControl+";

  public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

  public static Identifier identifier(String path) {
    return new Identifier(MOD_ID, path);
  }

  private static final MinecraftClient client = MinecraftClient.getInstance();

  private static ResolutionControlMod instance;

  public static ResolutionControlMod getInstance() {
    return instance;
  }

  private KeyBinding settingsKey;

  private boolean shouldScale = false;

  @Nullable private Framebuffer framebuffer;

  @Nullable private Framebuffer clientFramebuffer;

  private Class<? extends SettingsScreen> lastSettingsScreen = MainSettingsScreen.class;

  private int currentWidth;
  private int currentHeight;

  private long estimatedMemory;

  @Override
  public void onInitialize() {
    instance = this;

    Config.loadConfig();

    settingsKey =
        KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.resolutioncontrol.settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "key.categories.resolutioncontrol"));

    ClientTickEvents.END_CLIENT_TICK.register(
        client -> {
          while (settingsKey.wasPressed()) {
            client.setScreen(SettingsScreen.getScreen(lastSettingsScreen));
          }
        });
  }

  public void setShouldScale(boolean shouldScale) {
    if (shouldScale == this.shouldScale) return;

    //		if (getScaleFactor() == 1) return;

    Window window = getWindow();
    if (framebuffer == null) {
      this.shouldScale = true; // so we get the right dimensions
      framebuffer =
          new WindowFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight());
      calculateSize();
    }

    this.shouldScale = shouldScale;

    // swap out framebuffers as needed
    if (shouldScale) {
      clientFramebuffer = client.getFramebuffer();

      setClientFramebuffer(framebuffer);

      framebuffer.beginWrite(true);
      // nothing on the client's framebuffer yet
    } else {
      setClientFramebuffer(clientFramebuffer);
      client.getFramebuffer().beginWrite(true);
      framebuffer.draw(window.getFramebufferWidth(), window.getFramebufferHeight());
    }
  }

  public void initMinecraftFramebuffers() {
    if (minecraftFramebuffers != null) {
      minecraftFramebuffers.clear();
    } else {
      minecraftFramebuffers = new HashSet<>();
    }

    minecraftFramebuffers.add(client.worldRenderer.getEntityOutlinesFramebuffer());
    minecraftFramebuffers.add(client.worldRenderer.getTranslucentFramebuffer());
    minecraftFramebuffers.add(client.worldRenderer.getEntityFramebuffer());
    minecraftFramebuffers.add(client.worldRenderer.getParticlesFramebuffer());
    minecraftFramebuffers.add(client.worldRenderer.getWeatherFramebuffer());
    minecraftFramebuffers.add(client.worldRenderer.getCloudsFramebuffer());
    minecraftFramebuffers.remove(null);
  }

  public Framebuffer getFramebuffer() {
    return framebuffer;
  }

  public void setScaleFactor(float scaleFactor) {
    Config.setScaleFactor(scaleFactor);

    updateFramebufferSize();
  }

  public void setUpscaleAlgorithm(ScalingAlgorithm algorithm) {
    if (algorithm == Config.getUpscaleAlgorithm()) return;

    Config.setUpscaleAlgorithm(algorithm);

    onResolutionChanged();
  }

  public void nextUpscaleAlgorithm() {
    ScalingAlgorithm currentAlgorithm = Config.getUpscaleAlgorithm();
    if (currentAlgorithm.equals(ScalingAlgorithm.NEAREST)) {
      setUpscaleAlgorithm(ScalingAlgorithm.LINEAR);
    } else {
      setUpscaleAlgorithm(ScalingAlgorithm.NEAREST);
    }
  }

  public void setDownscaleAlgorithm(ScalingAlgorithm algorithm) {
    if (algorithm == Config.getDownscaleAlgorithm()) return;

    Config.setDownscaleAlgorithm(algorithm);

    onResolutionChanged();
  }

  public void nextDownscaleAlgorithm() {
    ScalingAlgorithm currentAlgorithm = Config.getDownscaleAlgorithm();
    if (currentAlgorithm.equals(ScalingAlgorithm.NEAREST)) {
      setDownscaleAlgorithm(ScalingAlgorithm.LINEAR);
    } else {
      setDownscaleAlgorithm(ScalingAlgorithm.NEAREST);
    }
  }

  public double getCurrentScaleFactor() {
    return shouldScale ? Config.getScaleFactor() : 1;
  }

  public void onResolutionChanged() {
    if (getWindow() == null) return;

    LOGGER.info(
        "Size changed to {}x{} {}x{} {}x{}",
        getWindow().getFramebufferWidth(),
        getWindow().getFramebufferHeight(),
        getWindow().getWidth(),
        getWindow().getHeight(),
        getWindow().getScaledWidth(),
        getWindow().getScaledHeight());

    //		if (getWindow().getScaledHeight() == lastWidth
    //				|| getWindow().getScaledHeight() == lastHeight)
    //		{
    updateFramebufferSize();

    //		}
  }

  public void updateFramebufferSize() {
    if (framebuffer == null) return;

    resize(framebuffer);
    resizeEntityOutlinesFramebuffer();

    calculateSize();
  }

  /**
   * For reasons that I cannot begin to understand or explain, resizing what is labeled as the
   * EntityOutlinesFramebuffer fixes entities not correctly rendering when the framebuffer is
   * resized. So things like the player's hand, nether portals, clouds and mobs will not render at
   * the proper coordinates if the framebuffer is resized and this is not called. The actual in game
   * blocks that are rendered are not affected by this.
   */
  public void resizeEntityOutlinesFramebuffer() {
    resize((client.worldRenderer.getEntityOutlinesFramebuffer()));
  }

  public void calculateSize() {
    currentWidth = framebuffer.textureWidth;
    currentHeight = framebuffer.textureHeight;

    // Framebuffer uses color (4 x 8 = 32 bit int) and depth (32 bit float)
    estimatedMemory = ((long) currentWidth * currentHeight * 24) / 8;
  }

  public void resize(@Nullable Framebuffer framebuffer) {
    if (framebuffer == null) return;

    boolean prev = this.shouldScale;
    this.shouldScale = true;
    framebuffer.resize(
        getWindow().getFramebufferWidth(),
        getWindow().getFramebufferHeight(),
        MinecraftClient.IS_SYSTEM_MAC);
    this.shouldScale = prev;
  }

  private Window getWindow() {
    return client.getWindow();
  }

  private void setClientFramebuffer(Framebuffer framebuffer) {
    client.framebuffer = framebuffer;
  }

  public KeyBinding getSettingsKey() {
    return settingsKey;
  }

  public int getCurrentWidth() {
    return currentWidth;
  }

  public int getCurrentHeight() {
    return currentHeight;
  }

  public String getEstimatedMemory() {
    if (estimatedMemory < 1000) {
      return estimatedMemory + " B";
    }

    String[] units = {"KB", "MB", "GB", "TB", "PB", "EB"};

    int log10 = (int) Math.log10(estimatedMemory);
    int decimalPlace = (int) Math.pow(10, 2 - log10 % 3);
    int displayDigits = (int) (estimatedMemory / Math.pow(10, log10 - 2));

    float result = (float) displayDigits / decimalPlace;
    return String.format("%s %s", result, units[log10 / 3 - 1]);
  }

  public void setLastSettingsScreen(Class<? extends SettingsScreen> ordinal) {
    this.lastSettingsScreen = ordinal;
  }
}
