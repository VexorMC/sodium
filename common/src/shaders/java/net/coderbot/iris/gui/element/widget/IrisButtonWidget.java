package net.coderbot.iris.gui.element.widget;

import net.minecraft.client.gui.widget.ButtonWidget;

import java.util.function.Consumer;

public class IrisButtonWidget extends ButtonWidget {
    protected final Consumer<IrisButtonWidget> callback;

    public IrisButtonWidget(int x, int y, int width, int height, String message, Consumer<IrisButtonWidget> callback) {
        super(69420, x, y, width, height, message);

        this.callback = callback;
    }

    public void click() {
        this.callback.accept(this);
    }
}