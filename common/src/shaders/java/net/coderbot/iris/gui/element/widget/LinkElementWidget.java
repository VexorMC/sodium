package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuLinkElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public class LinkElementWidget extends CommentedElementWidget<OptionMenuLinkElement> {
	private static final Text ARROW = new LiteralText(">");

	private final String targetScreenId;
	private final Text label;

	private NavigationController navigation;
	private Text trimmedLabel = null;
	private boolean isLabelTrimmed = false;

	public LinkElementWidget(OptionMenuLinkElement element) {
		super(element);

		this.targetScreenId = element.targetScreenId;
		this.label = GuiUtil.translateOrDefault(new LiteralText(element.targetScreenId), "screen." + element.targetScreenId);
	}

	@Override
	public void init(ShaderPackScreen screen, NavigationController navigation) {
		this.navigation = navigation;
	}

	@Override
	public void render(int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		GuiUtil.bindIrisWidgetsTexture();
		GuiUtil.drawButton(x, y, width, height, hovered, false);

		TextRenderer font = MinecraftClient.getInstance().textRenderer;

		int maxLabelWidth = width - 9;

		if (font.getStringWidth(this.label.asFormattedString()) > maxLabelWidth) {
			this.isLabelTrimmed = true;
		}

		if (this.trimmedLabel == null) {
			this.trimmedLabel = GuiUtil.shortenText(font, this.label, maxLabelWidth);
		}

		int labelWidth = font.getStringWidth(this.trimmedLabel.asFormattedString());

		font.drawWithShadow(this.trimmedLabel.asFormattedString(), x + (int)(width * 0.5) - (int)(labelWidth * 0.5) - (int)(0.5 * Math.max(labelWidth - (width - 18), 0)), y + 7, 0xFFFFFF);
		font.draw(ARROW.asFormattedString(), (x + width) - 9, y + 7, 0xFFFFFF);

		if (hovered && this.isLabelTrimmed) {
			// To prevent other elements from being drawn on top of the tooltip
			ShaderPackScreen.TOP_LAYER_RENDER_QUEUE.add(() -> GuiUtil.drawTextPanel(font, this.label, mouseX + 2, mouseY - 16));
		}
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
			this.navigation.open(targetScreenId);
			GuiUtil.playButtonClickSound();

			return true;
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public Optional<Text> getCommentTitle() {
		return Optional.of(this.label);
	}

	@Override
	public Optional<Text> getCommentBody() {
		String translation = "screen." + this.targetScreenId + ".comment";
		return Optional.of(new TranslatableText(translation));
	}
}
