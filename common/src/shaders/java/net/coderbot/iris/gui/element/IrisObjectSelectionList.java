package net.coderbot.iris.gui.element;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EntryListWidget;

public abstract class IrisObjectSelectionList extends EntryListWidget {
	public IrisObjectSelectionList(MinecraftClient client, int width, int height, int top, int bottom, int left, int right, int itemHeight) {
		super(client, width, height, top, bottom, itemHeight);
        this.centerListVertically = false;
		this.xStart = left;
		this.xEnd = right;
	}

    @Override
	protected int getScrollbarPosition() {
		// Position the scrollbar at the rightmost edge of the screen.
		// By default, the scrollbar is positioned moderately offset from the center.
		return width - 6;
	}
}
