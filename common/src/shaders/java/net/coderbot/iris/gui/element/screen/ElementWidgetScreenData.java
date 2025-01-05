package net.coderbot.iris.gui.element.screen;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class ElementWidgetScreenData {
	public static final ElementWidgetScreenData EMPTY = new ElementWidgetScreenData(new LiteralText(""), true);

	public final Text heading;
	public final boolean backButton;

	public ElementWidgetScreenData(Text heading, boolean backButton) {
		this.heading = heading;
		this.backButton = backButton;
	}
}
