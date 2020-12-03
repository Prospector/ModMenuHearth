package io.github.prospector.modmenuhearth.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

import java.util.List;

public class RenderUtils {
    private static final Minecraft CLIENT = Minecraft.getInstance();

    public static void drawWrappedString(MatrixStack matrices, String string, int x, int y, int wrapWidth, int lines, int color) {
        while (string != null && string.endsWith("\n")) {
            string = string.substring(0, string.length() - 1);
        }
        List<ITextProperties> strings = CLIENT.textRenderer.getTextHandler().wrapLines(new StringTextComponent(string), wrapWidth, Style.EMPTY);
        for (int i = 0; i < strings.size(); i++) {
            if (i >= lines) {
                break;
            }
            ITextProperties renderable = strings.get(i);
            if (i == lines - 1 && strings.size() > lines) {
                renderable = ITextProperties.concat(strings.get(i), ITextProperties.plain("..."));
            }
            IReorderingProcessor line = LanguageMap.getInstance().reorder(renderable);
            int x1 = x;
            if (CLIENT.textRenderer.isRightToLeft()) {
                int width = CLIENT.textRenderer.getWidth(line);
                x1 += (float) (wrapWidth - width);
            }
            CLIENT.textRenderer.draw(matrices, line, x1, y + i * CLIENT.textRenderer.fontHeight, color);
        }
    }

    public static void drawBadge(MatrixStack matrices, int x, int y, int tagWidth, IReorderingProcessor text, int outlineColor, int fillColor, int textColor) {
        AbstractGui.fill(matrices, x + 1, y - 1, x + tagWidth, y, outlineColor);
        AbstractGui.fill(matrices, x, y, x + 1, y + CLIENT.textRenderer.fontHeight, outlineColor);
        AbstractGui.fill(matrices, x + 1, y + 1 + CLIENT.textRenderer.fontHeight - 1, x + tagWidth, y + CLIENT.textRenderer.fontHeight + 1, outlineColor);
        AbstractGui.fill(matrices, x + tagWidth, y, x + tagWidth + 1, y + CLIENT.textRenderer.fontHeight, outlineColor);
        AbstractGui.fill(matrices, x + 1, y, x + tagWidth, y + CLIENT.textRenderer.fontHeight, fillColor);
        CLIENT.textRenderer.draw(matrices, text, (x + 1 + (tagWidth - CLIENT.textRenderer.getWidth(text)) / (float) 2), y + 1, textColor);
    }
}
