package io.github.ultimateboomer.resolutioncontrol.util;

public final class Config {
	public float scaleFactor = 1.0f;

	public ScalingAlgorithm upscaleAlgorithm = ScalingAlgorithm.NEAREST;
	public ScalingAlgorithm downscaleAlgorithm = ScalingAlgorithm.LINEAR;

	public boolean mipmapHighRes = false;

	public static Config getInstance() {
		return ConfigHandler.instance.getConfig();
	}

}
