package io.github.ultimateboomer.resolutioncontrol;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import io.github.ultimateboomer.resolutioncontrol.client.gui.screen.MainSettingsScreen;
import io.github.ultimateboomer.resolutioncontrol.client.gui.screen.SettingsScreen;
import io.github.ultimateboomer.resolutioncontrol.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class ResolutionControlMod implements ModInitializer {
  public static final String MOD_ID = "resolutioncontrol";
  public static final String MOD_NAME = "ResolutionControl+";

  public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

  public static ResourceLocation identifier(String path) {
    return new ResourceLocation(MOD_ID, path);
  }

  private static final Minecraft client = Minecraft.getInstance();

  private static ResolutionControlMod instance;

  public static ResolutionControlMod getInstance() {
    return instance;
  }

  private KeyMapping settingsKey;

  private boolean shouldScale = false;

  public RenderTarget framebuffer;

  @Nullable private RenderTarget clientFramebuffer;

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
            new KeyMapping(
                "key.resolutioncontrol.settings",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "key.categories.resolutioncontrol"));

    ClientTickEvents.END_CLIENT_TICK.register(
        client -> {
          while (settingsKey.consumeClick()) {
            client.setScreen(SettingsScreen.getScreen(lastSettingsScreen));
          }
        });

    ClientLifecycleEvents.CLIENT_STARTED.register(
        client -> {
          framebuffer =
              new MainTarget(client.getWindow().getWidth(), client.getWindow().getHeight());
          resize(framebuffer);
          calculateSize();
        });
  }

  public void setShouldScale(boolean shouldScale) {
    if (shouldScale == this.shouldScale) return;

    this.shouldScale = shouldScale;

    // swap out framebuffers as needed
    if (shouldScale) {
      clientFramebuffer = client.getMainRenderTarget();
      client.mainRenderTarget = framebuffer;
      framebuffer.bindWrite(true);
      // nothing on the client's framebuffer yet
    } else {
      client.mainRenderTarget = clientFramebuffer;
      client.getMainRenderTarget().bindWrite(true);
      var window = getWindow();
      framebuffer.blitToScreen(window.getWidth(), window.getHeight());
    }
  }

  public void setScaleFactor(float scaleFactor) {
    Config.setScaleFactor(scaleFactor);

    updateFramebufferSize();
  }

  public double getCurrentScaleFactor() {
    return shouldScale ? Config.getScaleFactor() : 1;
  }

  public void updateFramebufferSize() {
    if (getWindow() == null) return;

    resize(framebuffer);
    resizeEntityOutlinesFramebuffer();

    var caller = Thread.currentThread();
    LOGGER.info(
        "Size changed to {}x{} {}x{} {}x{} by {}",
        framebuffer.width,
        framebuffer.height,
        getWindow().getWidth(),
        getWindow().getHeight(),
        getWindow().getGuiScaledWidth(),
        getWindow().getGuiScaledHeight(),
        caller.getName());
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
    resize((client.levelRenderer.entityTarget()));
  }

  public void calculateSize() {
    currentWidth = framebuffer.width;
    currentHeight = framebuffer.height;

    // Framebuffer uses 24 bits per pixel (8 bits per channel)
    estimatedMemory = ((long) currentWidth * currentHeight * 24) / 8;
  }

  public void resize(@Nullable RenderTarget framebuffer) {
    if (framebuffer == null) return;

    boolean prev = this.shouldScale;
    this.shouldScale = true;
    framebuffer.resize(getWindow().getWidth(), getWindow().getHeight(), Minecraft.ON_OSX);
    this.shouldScale = prev;
  }

  private Window getWindow() {
    return client.getWindow();
  }

  public KeyMapping getSettingsKey() {
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
