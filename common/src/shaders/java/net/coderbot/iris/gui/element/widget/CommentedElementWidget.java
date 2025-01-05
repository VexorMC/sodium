package net.coderbot.iris.gui.element.widget;

import net.coderbot.iris.shaderpack.option.menu.OptionMenuElement;
import net.minecraft.text.Text;

import java.util.Optional;

public abstract class CommentedElementWidget<T extends OptionMenuElement> extends AbstractElementWidget<T> {
	public CommentedElementWidget(T element) {
		super(element);
	}

	public abstract Optional<Text> getCommentTitle();

	public abstract Optional<Text> getCommentBody();
}
