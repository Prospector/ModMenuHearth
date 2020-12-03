package io.github.prospector.modmenuhearth.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class ModMenuTexturedButtonWidget extends Button {
    private final ResourceLocation texture;
    private final int u;
    private final int v;
    private final int uWidth;
    private final int vHeight;

    protected ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, ResourceLocation texture, IPressable onPress) {
        this(x, y, width, height, u, v, texture, 256, 256, onPress);
    }

    protected ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, ResourceLocation texture, int uWidth, int vHeight, IPressable onPress) {
        this(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, NarratorChatListener.EMPTY);
    }

    protected ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, ResourceLocation texture, int uWidth, int vHeight, IPressable onPress, ITextComponent message) {
        this(x, y, width, height, u, v, texture, uWidth, vHeight, onPress, message, EMPTY);
    }

    protected ModMenuTexturedButtonWidget(int x, int y, int width, int height, int u, int v, ResourceLocation texture, int uWidth, int vHeight, IPressable onPress, ITextComponent message, ITooltip tooltipSupplier) {
        super(x, y, width, height, message, onPress, tooltipSupplier);
        this.uWidth = uWidth;
        this.vHeight = vHeight;
        this.u = u;
        this.v = v;
        this.texture = texture;
    }

    protected void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        Minecraft client = Minecraft.getInstance();
        client.getTextureManager().bindTexture(this.texture);
        RenderSystem.color4f(1, 1, 1, 1f);
        RenderSystem.disableDepthTest();
        int adjustedV = this.v;
        if (!active) {
            adjustedV += this.height * 2;
        } else if (this.isHovered()) {
            adjustedV += this.height;
        }

        drawTexture(matrices, this.x, this.y, this.u, adjustedV, this.width, this.height, this.uWidth, this.vHeight);
        RenderSystem.enableDepthTest();

        if (this.isHovered()) {
            this.renderToolTip(matrices, mouseX, mouseY);
        }
    }

    public boolean isJustHovered() {
        return hovered;
    }

    public boolean isFocusedButNotHovered() {
        return !hovered && isFocused();
    }
}
