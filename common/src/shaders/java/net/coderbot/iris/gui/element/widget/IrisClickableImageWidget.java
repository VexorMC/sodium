package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL14;

import java.util.function.Consumer;

public class IrisClickableImageWidget extends IrisButtonWidget {
    private final Identifier texture;
    private final int xTexStart;
    private final int yTexStart;
    private final int yDiffTex;

    public IrisClickableImageWidget(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex, Identifier texture, Consumer<IrisButtonWidget> onPress) {
        super(x, y, width, height, "", onPress);

        this.texture = texture;
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.yDiffTex = yDiffTex;
    }

    @Override
    public void render(MinecraftClient client, int mouseX, int mouseY) {
        if(!this.visible) {
            return;
        }

        int yTex = this.yTexStart;
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        if(this.getYImage(hovered) == 2) {
            yTex += this.yDiffTex;
        }

        GL14.glBlendColor(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableBlend();

        GlStateManager.enableTexture();
        client.getTextureManager().bindTexture(this.texture);

        drawTexture(this.x, this.y, this.xTexStart, yTex, width, height);
    }
}