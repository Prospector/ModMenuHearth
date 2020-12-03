package io.github.prospector.modmenuhearth.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TextComponent;

public class ModMenuButtonWidget extends Button {
    public ModMenuButtonWidget(int x, int y, int width, int height, TextComponent text, Screen screen) {
        super(x, y, width, height, text, button -> Minecraft.getInstance().openScreen(new ModsScreen(screen)));
    }
}
