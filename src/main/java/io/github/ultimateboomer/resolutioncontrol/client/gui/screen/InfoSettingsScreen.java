package io.github.ultimateboomer.resolutioncontrol.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class InfoSettingsScreen extends SettingsScreen {
  private String gpuName;
  private int maxTextureSize;

  protected InfoSettingsScreen(@Nullable Screen parent) {
    super(text("settings.info"), parent);
  }

  @Override
  protected void init() {
    super.init();

    this.gpuName = GL11.glGetString(GL11.GL_RENDERER);
    this.maxTextureSize = RenderSystem.maxSupportedTextureSize();
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    super.render(context, mouseX, mouseY, delta);

    drawLeftAlignedString(
        context, text("settings.info.gpu").getString(), centerX - 85, centerY - 25, 0x808080);
    drawMultilineString(
        context,
        MultilineText.create(textRenderer, Text.literal(gpuName), 150),
        centerX - 60,
        centerY - 25,
        0x808080);

    drawLeftAlignedString(
        context,
        text("settings.info.maxTextureSize").getString() + " " + maxTextureSize,
        centerX - 85,
        centerY - 40,
        0x808080);
  }
}
