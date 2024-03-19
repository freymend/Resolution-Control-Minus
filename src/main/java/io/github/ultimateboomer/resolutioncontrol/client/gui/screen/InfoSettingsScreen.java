package io.github.ultimateboomer.resolutioncontrol.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class InfoSettingsScreen extends SettingsScreen {
  private String gpuName;
  private int maxTextureSize;

  protected InfoSettingsScreen(@Nullable Screen parent) {
    super(SettingsScreen.text("settings.info"), parent);
  }

  @Override
  protected void init() {
    super.init();

    this.gpuName = GL11.glGetString(GL11.GL_RENDERER);
    this.maxTextureSize = RenderSystem.maxSupportedTextureSize();
  }

  @Override
  public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
    super.render(context, mouseX, mouseY, delta);

    super.drawLeftAlignedString(
        context, text("settings.info.gpu").getString(), centerX - 85, centerY - 25, 0x808080);
    super.drawMultilineString(
        context,
        MultiLineLabel.create(font, Component.literal(gpuName), 150),
        centerX - 60,
        centerY - 25,
        0x808080);

    super.drawLeftAlignedString(
        context,
        text("settings.info.maxTextureSize").getString() + " " + maxTextureSize,
        centerX - 85,
        centerY - 40,
        0x808080);
  }
}
