package io.github.prospector.modmenuhearth.util;

import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public enum BadgeType {
    LIBRARY(new TranslationTextComponent("modmenu.library"), 0xff107454, 0xff093929),
    CLIENTSIDE(new TranslationTextComponent("modmenu.clientsideOnly"), 0xff2b4b7c, 0xff0e2a55),
    DEPRECATED(new TranslationTextComponent("modmenu.deprecated"), 0xffff3333, 0xffb30000),
    PATCHWORK_FORGE(new TranslationTextComponent("modmenu.forge"), 0xff1f2d42, 0xff101721),
    MINECRAFT(new TranslationTextComponent("modmenu.minecraft"), 0xff6f6c6a, 0xff31302f);

    private TextComponent text;
    private int outlineColor;
    private int fillColor;

    private BadgeType(TextComponent text, int outlineColor, int fillColor) {
        this.text = text;
        this.outlineColor = outlineColor;
        this.fillColor = fillColor;
    }

    public TextComponent getText() {
        return this.text;
    }

    public int getOutlineColor() {
        return this.outlineColor;
    }

    public int getFillColor() {
        return this.fillColor;
    }
}
