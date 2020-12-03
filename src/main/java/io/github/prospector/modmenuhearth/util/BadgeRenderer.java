package io.github.prospector.modmenuhearth.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.prospector.modmenuhearth.ModMenu;
import io.github.prospector.modmenuhearth.gui.ModsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.util.Calendar;

public class BadgeRenderer {
    protected int startX, startY, badgeX, badgeY, badgeMax;
    protected ModInfo metadata;
    protected Minecraft client;
    protected final ModsScreen screen;

    public BadgeRenderer(int startX, int startY, int endX, ModInfo metadata, ModsScreen screen) {
        this.startX = startX;
        this.startY = startY;
        this.badgeMax = endX;
        this.metadata = metadata;
        this.screen = screen;
        this.client = Minecraft.getInstance();
    }

    public void draw(MatrixStack matrices, int mouseX, int mouseY) {
        this.badgeX = startX;
        this.badgeY = startY;
        if (ModMenu.LIBRARY_MODS.contains(metadata.getModId())) {
            drawBadge(matrices, BadgeType.LIBRARY, mouseX, mouseY);
        }
        if (ModMenu.CLIENTSIDE_MODS.contains(metadata.getModId())) {
            drawBadge(matrices, BadgeType.CLIENTSIDE, mouseX, mouseY);
        }
        if (ModMenu.DEPRECATED_MODS.contains(metadata.getModId())) {
            drawBadge(matrices, BadgeType.DEPRECATED, mouseX, mouseY);
        }
        if (ModMenu.PATCHWORK_FORGE_MODS.contains(metadata.getModId())) {
            drawBadge(matrices, BadgeType.PATCHWORK_FORGE, mouseX, mouseY);
        }
        if (metadata.getModId().equals("minecraft")) {
            drawBadge(matrices, BadgeType.MINECRAFT, mouseX, mouseY);
        }
        //noinspection MagicConstant
        if (Calendar.getInstance().get(0b10) == 0b11 && Calendar.getInstance().get(0b101) == 0x1) {
            if (metadata.getModId().equals(new String(new byte[]{109, 111, 100, 109, 101, 110, 117}))) {
                drawBadge(matrices, new StringTextComponent(new String(new byte[]{-30, -100, -104, 32, 86, 105, 114, 117, 115, 32, 68, 101, 116, 101, 99, 116, 101, 100})).asOrderedText(), 0b10001000111111110010001000100010, 0b10001000011111110000100000001000, mouseX, mouseY);
            } else if (metadata.getModId().contains(new String(new byte[]{116, 97, 116, 101, 114}))) {
                drawBadge(matrices, new StringTextComponent(new String(new byte[]{116, 97, 116, 101, 114})).asOrderedText(), 0b10001000111010111011001100101011, 0b10001000100110010111000100010010, mouseX, mouseY);
            } else {
                drawBadge(matrices, new StringTextComponent(new String(new byte[]{-30, -100, -108, 32, 98, 121, 32, 77, 99, 65, 102, 101, 101})).asOrderedText(), 0b10001000000111011111111101001000, 0b10001000000001110110100100001110, mouseX, mouseY);
            }
        }
    }

    public void drawBadge(MatrixStack matrices, BadgeType badgeType, int mouseX, int mouseY) {
        this.drawBadge(matrices, badgeType.getText().asOrderedText(), badgeType.getOutlineColor(), badgeType.getFillColor(), mouseX, mouseY);
    }

    public void drawBadge(MatrixStack matrices, IReorderingProcessor text, int outlineColor, int fillColor, int mouseX, int mouseY) {
        int width = client.textRenderer.getWidth(text) + 6;
        if (badgeX + width < badgeMax) {
            RenderUtils.drawBadge(matrices, badgeX, badgeY, width, text, outlineColor, fillColor, 0xCACACA);
            badgeX += width + 3;
        }
    }

    public ModInfo getMetadata() {
        return metadata;
    }
}
