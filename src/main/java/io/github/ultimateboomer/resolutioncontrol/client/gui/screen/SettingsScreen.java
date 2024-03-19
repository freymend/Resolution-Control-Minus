package io.github.ultimateboomer.resolutioncontrol.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.ultimateboomer.resolutioncontrol.ResolutionControlMod;
import io.github.ultimateboomer.resolutioncontrol.util.Config;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
@SuppressWarnings("StaticInitializerReferencesSubClass")
public class SettingsScreen extends Screen {
  protected static final ResourceLocation WINDOW_TEXTURE =
      ResolutionControlMod.identifier("textures/gui/settings.png");

  protected static Component text(String path, Object... args) {
    return Component.translatable(ResolutionControlMod.MOD_ID + "." + path, args);
  }

  protected static final int containerWidth = 192;
  protected static final int containerHeight = 128;

  protected static final Map<Class<? extends SettingsScreen>, Function<Screen, SettingsScreen>>
      screensSupplierList;

  static {
    screensSupplierList = new LinkedHashMap<>();
    screensSupplierList.put(MainSettingsScreen.class, MainSettingsScreen::new);
    screensSupplierList.put(InfoSettingsScreen.class, InfoSettingsScreen::new);
  }

  protected final ResolutionControlMod mod = ResolutionControlMod.getInstance();

  @Nullable protected final Screen parent;

  protected int centerX;
  protected int centerY;
  protected int startX;
  protected int startY;

  protected Map<Class<? extends SettingsScreen>, Button> menuButtons;

  protected Button doneButton;

  protected SettingsScreen(Component title, @Nullable Screen parent) {
    super(title);
    this.parent = parent;
  }

  @Override
  protected void init() {
    if (super.minecraft == null) throw new IllegalStateException("Client is missing, are we a server?");
    super.init();

    centerX = width / 2;
    centerY = height / 2;
    startX = centerX - containerWidth / 2;
    startY = centerY - containerHeight / 2;

    // Init menu buttons
    menuButtons = new LinkedHashMap<>();
    final int menuButtonWidth = 80;
    final int menuButtonHeight = 20;
    final int[] offset = {0}; // lambda needs effectively final variable

    screensSupplierList.forEach(
        (c, constructor) -> {
          SettingsScreen r = constructor.apply(this.parent);
          var b =
              new Button.Builder(
                      r.getTitle(), button -> super.minecraft.setScreen(constructor.apply(this.parent)))
                  .bounds(
                      startX - menuButtonWidth - 20,
                      startY + offset[0],
                      menuButtonWidth,
                      menuButtonHeight)
                  .build();

          if (this.getClass().equals(c)) b.active = false;

          menuButtons.put(c, b);
          offset[0] += 25;
        });

    menuButtons.values().forEach(this::addRenderableWidget);

    doneButton =
        new Button.Builder(
                Component.translatable("gui.done"),
                button -> {
                  applySettingsAndCleanup();
                  minecraft.setScreen(this.parent);
                })
            .bounds(centerX + 15, startY + containerHeight - 30, 60, 20)
            .build();
    super.addRenderableWidget(doneButton);
  }

  @Override
  public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
    if (super.minecraft.level == null) {
      super.renderDirtBackground(context);
    }

    RenderSystem.disableDepthTest();
    RenderSystem.setShaderTexture(0, WINDOW_TEXTURE);
    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

    int textureWidth = 256;
    int textureHeight = 192;
    context.blit(
        WINDOW_TEXTURE,
        centerX - textureWidth / 2,
        centerY - textureHeight / 2,
        0,
        0,
        textureWidth,
        textureHeight);

    super.render(context, mouseX, mouseY, delta);

    drawLeftAlignedString(
        context, "\u00a7r" + super.getTitle().getString(), centerX + 15, startY + 10, 0x000000);

    drawRightAlignedString(
        context, text("settings.title").getString(), centerX + 5, startY + 10, 0x404040);
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if ((ResolutionControlMod.getInstance().getSettingsKey().matches(keyCode, scanCode))) {
      applySettingsAndCleanup();
      super.minecraft.setScreen(this.parent);
      super.minecraft.mouseHandler.grabMouse();
      return true;
    } else {
      return super.keyPressed(keyCode, scanCode, modifiers);
    }
  }

  @Override
  public void onClose() {
    applySettingsAndCleanup();
    super.onClose();
  }

  protected void applySettingsAndCleanup() {
    Config.saveConfig();
    mod.setLastSettingsScreen(this.getClass());
  }

  protected void drawCenteredString(GuiGraphics context, String text, int x, int y, int color) {
    context.drawString(super.font, text, x - super.font.width(text) / 2, y, color, false);
  }

  protected void drawLeftAlignedString(GuiGraphics context, String text, int x, int y, int color) {
    context.drawString(super.font, text, x, y, color, false);
  }

  protected  void drawMultilineString(GuiGraphics context, MultiLineLabel text, int x, int y, int color) {
    text.renderLeftAlignedNoShadow(context, x, y, 16, color);
  }

  protected void drawRightAlignedString(GuiGraphics context, String text, int x, int y, int color) {
    context.drawString(super.font, text, x - super.font.width(text), y, color, false);
  }

  public static SettingsScreen getScreen(Class<? extends SettingsScreen> screenClass) {
    return screensSupplierList.get(screenClass).apply(null);
  }
}
