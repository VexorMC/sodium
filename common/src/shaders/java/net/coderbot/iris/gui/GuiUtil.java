package net.coderbot.iris.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

/**
 * Class serving as abstraction and
 * centralization for common GUI
 * rendering/other code calls.
 *
 * Helps allow for easier portability
 * to Minecraft 1.17 by abstracting
 * some code that will be changed.
 */
public final class GuiUtil {
    public static final Identifier IRIS_WIDGETS_TEX = new Identifier("iris", "textures/gui/widgets.png");
    private static final Text ELLIPSIS = new LiteralText("...");

    private GuiUtil() {
    }

    private static MinecraftClient client() {
        return MinecraftClient.getInstance();
    }

    /**
     * Binds Iris's widgets texture to be
     * used for succeeding draw calls.
     */
    public static void bindIrisWidgetsTexture() {
        client().getTextureManager().bindTexture(IRIS_WIDGETS_TEX);
    }

    /**
     * Draws a button. Button textures must be mapped with the
     * same coordinates as those on the vanilla widgets texture.
     *
     * @param x        X position of the left of the button
     * @param y        Y position of the top of the button
     * @param width    Width of the button, maximum 398
     * @param height   Height of the button, maximum 20
     * @param hovered  Whether the button is being hovered over with the mouse
     * @param disabled Whether the button should use the "disabled" texture
     */
    public static void drawButton(int x, int y, int width, int height, boolean hovered, boolean disabled) {
        // Create variables for half of the width and height.
        // Will not be exact when width and height are odd, but
        // that case is handled within the draw calls.
        int halfWidth = width / 2;
        int halfHeight = height / 2;

        // V offset for which button texture to use
        int vOffset = disabled ? 46 : hovered ? 86 : 66;

        // Sets RenderSystem to use solid white as the tint color for blend mode, and enables blend mode
        RenderSystem.blendColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();

        // Sets RenderSystem to be able to use textures when drawing
        RenderSystem.enableTexture();

        // Top left section
        drawTexture(x, y, 0, vOffset, halfWidth, halfHeight);
        // Top right section
        drawTexture(x + halfWidth, y, 200 - (width - halfWidth), vOffset, width - halfWidth, halfHeight);
        // Bottom left section
        drawTexture(x, y + halfHeight, 0, vOffset + (20 - (height - halfHeight)), halfWidth, height - halfHeight);
        // Bottom right section
        drawTexture(x + halfWidth, y + halfHeight, 200 - (width - halfWidth), vOffset + (20 - (height - halfHeight)), width - halfWidth, height - halfHeight);
    }

    public static void drawTexture(int x, int y, int u, int v, int width, int height) {
        float f = 0.00390625F;
        float g = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex((double)(x + 0), (double)(y + height), (double)0.0D).texture((double)((float)(u + 0) * f), (double)((float)(v + height) * g)).next();
        bufferBuilder.vertex((double)(x + width), (double)(y + height), (double)0.0D).texture((double)((float)(u + width) * f), (double)((float)(v + height) * g)).next();
        bufferBuilder.vertex((double)(x + width), (double)(y + 0), (double)0.0D).texture((double)((float)(u + width) * f), (double)((float)(v + 0) * g)).next();
        bufferBuilder.vertex((double)(x + 0), (double)(y + 0), (double)0.0D).texture((double)((float)(u + 0) * f), (double)((float)(v + 0) * g)).next();
        tessellator.draw();
    }
    
	/**
	 * Draws a translucent black panel
	 * with a light border.
	 *
	 * @param x The x position of the panel
	 * @param y The y position of the panel
	 * @param width The width of the panel
	 * @param height The height of the panel
	 */
	public static void drawPanel(int x, int y, int width, int height) {
		int borderColor = 0xDEDEDEDE;
		int innerColor = 0xDE000000;

		// Top border section
		DrawableHelper.fill(x, y, x + width, y + 1, borderColor);
		// Bottom border section
		DrawableHelper.fill(x, (y + height) - 1, x + width, y + height, borderColor);
		// Left border section
		DrawableHelper.fill(x, y + 1, x + 1, (y + height) - 1, borderColor);
		// Right border section
		DrawableHelper.fill((x + width) - 1, y + 1, x + width, (y + height) - 1, borderColor);
		// Inner section
		DrawableHelper.fill(x + 1, y + 1, (x + width) - 1, (y + height) - 1, innerColor);
	}

	/**
	 * Draws a text with a panel behind it.
	 *
	 * @param text The text component to draw
	 * @param x The x position of the panel
	 * @param y The y position of the panel
	 */
	public static void drawTextPanel(TextRenderer font, Text text, int x, int y) {
		drawPanel(x, y, font.getStringWidth(text.asFormattedString()) + 8, 16);
		font.drawWithShadow(text.asFormattedString(), x + 4, y + 4, 0xFFFFFF);
	}

	/**
	 * Shorten a text to a specific length, adding an ellipsis (...)
	 * to the end if shortened.
	 *
	 * Text may lose formatting.
	 *
	 * @param font Font to use for determining the width of text
	 * @param text Text to shorten
	 * @param width Width to shorten text to
	 * @return a shortened text
	 */
	public static Text shortenText(TextRenderer font, Text text, int width) {
		if (font.getStringWidth(text.asFormattedString()) > width) {
			return new LiteralText(font.trimToWidth(text.asFormattedString(), width - font.getStringWidth(ELLIPSIS.asFormattedString()))).append(ELLIPSIS).setStyle(text.getStyle());
		}
		return text;
	}

	/**
	 * Creates a new translated text, if a translation
	 * is present. If not, will return the default text
	 * component passed.
	 *
	 * @param defaultText Default text to use if no translation is found
	 * @param translationDesc Translation key to try and use
	 * @param format Formatting arguments for the translated text, if created
	 * @return the translated text if found, otherwise the default provided
	 */
	public static Text translateOrDefault(Text defaultText, String translationDesc, Object ... format) {
		if (!I18n.translate(translationDesc).equals(translationDesc)) {
			return new TranslatableText(translationDesc, format);
		}
		return defaultText;
	}

	/**
	 * Plays the {@code UI_BUTTON_CLICK} sound event as a
	 * master sound effect.
	 *
	 * Used in non-{@code ButtonWidget} UI elements upon click
	 * or other action.
	 */
	public static void playButtonClickSound() {
		client().getSoundManager().play(PositionedSoundInstance.master(new Identifier("gui.button.press"), 1.0F));
	}

	/**
	 * A class representing a section of a
	 * texture, to be easily drawn in GUIs.
	 */
	public static class Icon {
		public static final Icon SEARCH = new Icon(0, 0, 7, 8);
		public static final Icon CLOSE = new Icon(7, 0, 5, 6);
		public static final Icon REFRESH = new Icon(12, 0, 10, 10);
		public static final Icon EXPORT = new Icon(22, 0, 7, 8);
		public static final Icon EXPORT_COLORED = new Icon(29, 0, 7, 8);
		public static final Icon IMPORT = new Icon(22, 8, 7, 8);
		public static final Icon IMPORT_COLORED = new Icon(29, 8, 7, 8);

		private final int u;
		private final int v;
		private final int width;
		private final int height;

		public Icon(int u, int v, int width, int height) {
			this.u = u;
			this.v = v;
			this.width = width;
			this.height = height;
		}

		/**
		 * Draws this icon to the screen at the specified coordinates.
		 *
		 * @param x The x position to draw the icon at (left)
		 * @param y The y position to draw the icon at (top)
		 */
		public void draw(PoseStack poseStack, int x, int y) {
			// Sets RenderSystem to use solid white as the tint color for blend mode, and enables blend mode
			RenderSystem.blendColor(1.0f, 1.0f, 1.0f, 1.0f);
			RenderSystem.enableBlend();

			// Sets RenderSystem to be able to use textures when drawing
			RenderSystem.enableTexture();

			// Draw the texture to the screen
			DrawableHelper.drawTexture(x, y, u, v, width, height, 256, 256);
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}
	}
}
