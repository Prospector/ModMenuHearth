package io.github.prospector.modmenuhearth.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.prospector.modmenuhearth.util.HardcodedUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.StringTextComponent;

public class DescriptionListWidget extends AbstractList<DescriptionListWidget.DescriptionEntry> {

    private final ModsScreen parent;
    private final FontRenderer textRenderer;
    private ModListEntry lastSelected = null;

    public DescriptionListWidget(Minecraft client, int width, int height, int top, int bottom, int entryHeight, ModsScreen parent) {
        super(client, width, height, top, bottom, entryHeight);
        this.parent = parent;
        this.textRenderer = client.textRenderer;
    }

    @Override
    public DescriptionEntry getSelected() {
        return null;
    }

    @Override
    public int getRowWidth() {
        return this.width - 10;
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.width - 6 + left;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        ModListEntry selectedEntry = parent.getSelectedEntry();
        if (selectedEntry != lastSelected) {
            lastSelected = selectedEntry;
            clearEntries();
            setScrollAmount(-Double.MAX_VALUE);
            String description = lastSelected.getMetadata().getDescription();
            String id = lastSelected.getMetadata().getModId();
            if (description.isEmpty() && HardcodedUtil.getHardcodedDescriptions().containsKey(id)) {
                description = HardcodedUtil.getHardcodedDescription(id);
            }
            if (lastSelected != null && description != null && !description.isEmpty()) {
                for (IReorderingProcessor line : textRenderer.wrapLines(new StringTextComponent(description.replaceAll("\n", "\n\n")), getRowWidth())) {
                    children().add(new DescriptionEntry(line));
                }
            }
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.depthFunc(515);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.disableAlphaTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableTexture();

        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(this.left, (this.top + 4), 0.0D).texture(0.0F, 1.0F).color(0, 0, 0, 0).next();
        bufferBuilder.vertex(this.right, (this.top + 4), 0.0D).texture(1.0F, 1.0F).color(0, 0, 0, 0).next();
        bufferBuilder.vertex(this.right, this.top, 0.0D).texture(1.0F, 0.0F).color(0, 0, 0, 255).next();
        bufferBuilder.vertex(this.left, this.top, 0.0D).texture(0.0F, 0.0F).color(0, 0, 0, 255).next();
        bufferBuilder.vertex(this.left, this.bottom, 0.0D).texture(0.0F, 1.0F).color(0, 0, 0, 255).next();
        bufferBuilder.vertex(this.right, this.bottom, 0.0D).texture(1.0F, 1.0F).color(0, 0, 0, 255).next();
        bufferBuilder.vertex(this.right, (this.bottom - 4), 0.0D).texture(1.0F, 0.0F).color(0, 0, 0, 0).next();
        bufferBuilder.vertex(this.left, (this.bottom - 4), 0.0D).texture(0.0F, 0.0F).color(0, 0, 0, 0).next();
        tessellator.draw();

        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(this.left, this.bottom, 0.0D).color(0, 0, 0, 128).next();
        bufferBuilder.vertex(this.right, this.bottom, 0.0D).color(0, 0, 0, 128).next();
        bufferBuilder.vertex(this.right, this.top, 0.0D).color(0, 0, 0, 128).next();
        bufferBuilder.vertex(this.left, this.top, 0.0D).color(0, 0, 0, 128).next();
        tessellator.draw();

        int k = this.getRowLeft();
        int l = this.top + 4 - (int) this.getScrollAmount();
        this.renderList(matrices, k, l, mouseX, mouseY, delta);

        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
    }

    protected class DescriptionEntry extends AbstractList.AbstractListEntry<DescriptionEntry> {
        protected IReorderingProcessor text;

        public DescriptionEntry(IReorderingProcessor text) {
            this.text = text;
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            Minecraft.getInstance().textRenderer.drawWithShadow(matrices, text, x, y, 0xAAAAAA);
        }
    }

}
