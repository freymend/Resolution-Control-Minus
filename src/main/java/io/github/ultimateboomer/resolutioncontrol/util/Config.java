package io.github.ultimateboomer.resolutioncontrol.util;

import io.github.ultimateboomer.resolutioncontrol.ResolutionControlMod;
import java.io.*;
import java.util.Properties;
import net.minecraft.client.MinecraftClient;

public final class Config {

  private static float scaleFactor = 1.0f;
  private static ScalingAlgorithm upscaleAlgorithm = ScalingAlgorithm.NEAREST;
  private static ScalingAlgorithm downscaleAlgorithm = ScalingAlgorithm.LINEAR;

  private static boolean mipmapHighRes = false;

  private Config() {
    throw new IllegalStateException("Utility class");
  }

  public static void loadConfig() {
    var configFile = configFile();
    if (!configFile.exists()) {
      saveConfig();
    }

    var config = new Properties();
    try (var reader = new FileReader(configFile)) {
      config.load(reader);
    } catch (IOException e) {
      System.err.println("Could not load config file at " + configFile.getAbsolutePath());
      e.printStackTrace();
    }

    scaleFactor = Float.parseFloat(config.getProperty("scaleFactor", "1.0"));
    upscaleAlgorithm = ScalingAlgorithm.valueOf(config.getProperty("upscaleAlgorithm", "NEAREST"));
    downscaleAlgorithm =
        ScalingAlgorithm.valueOf(config.getProperty("downscaleAlgorithm", "LINEAR"));
    mipmapHighRes = Boolean.parseBoolean(config.getProperty("mipmapHighRes", "false"));
  }

  public static void saveConfig() {
    var configFile = configFile();
    if (configFile.getParentFile().mkdirs()) {
      System.out.println(
          "Created config directory at " + configFile.getParentFile().getAbsolutePath());
    }

    var config = new Properties();
    config.setProperty("scaleFactor", String.valueOf(scaleFactor));
    config.setProperty("upscaleAlgorithm", upscaleAlgorithm.name());
    config.setProperty("downscaleAlgorithm", downscaleAlgorithm.name());
    config.setProperty("mipmapHighRes", String.valueOf(mipmapHighRes));
    try (var writer = new FileWriter(configFile)) {
      config.store(writer, "Resolution Control Mod Config");
    } catch (IOException e) {
      System.err.println("Could not save config file at " + configFile.getAbsolutePath());
      e.printStackTrace();
    }
  }

  private static File configFile() {
    return new File(
        MinecraftClient.getInstance().runDirectory,
        "config/" + ResolutionControlMod.MOD_ID + ".properties");
  }

  public static float getScaleFactor() {
    return scaleFactor;
  }

  public static ScalingAlgorithm getUpscaleAlgorithm() {
    return upscaleAlgorithm;
  }

  public static ScalingAlgorithm getDownscaleAlgorithm() {
    return downscaleAlgorithm;
  }

  public static void setScaleFactor(float scaleFactor) {
    Config.scaleFactor = scaleFactor;
    saveConfig();
  }

  public static void setUpscaleAlgorithm(ScalingAlgorithm upscaleAlgorithm) {
    Config.upscaleAlgorithm = upscaleAlgorithm;
    saveConfig();
  }

  public static void setDownscaleAlgorithm(ScalingAlgorithm downscaleAlgorithm) {
    Config.downscaleAlgorithm = downscaleAlgorithm;
    saveConfig();
  }

  public static void setMipmapHighRes(boolean mipmapHighRes) {
    Config.mipmapHighRes = mipmapHighRes;
  }

  public static boolean getMipmapHighRes() {
    return mipmapHighRes;
  }
}
