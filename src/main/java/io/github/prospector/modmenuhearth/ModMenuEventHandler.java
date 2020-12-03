package io.github.prospector.modmenuhearth;

import io.github.prospector.modmenuhearth.gui.ModsScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ModMenuEventHandler {
    @SubscribeEvent
    public static void drawLast(GuiScreenEvent.InitGuiEvent event) {
        Screen screen = event.getGui();
        if (screen instanceof MainMenuScreen) {
            Optional<Widget> modsButton = event.getWidgetList().stream().filter(widget -> widget.getMessage().getString().equals(I18n.translate("fml.menu.mods"))).findFirst();
            if (modsButton.isPresent()) {
                Widget widget = modsButton.get();
                int x = widget.x;
                int y = widget.y;
                int width = widget.getWidth();
                int height = widget.unusedGetHeight();
                Button newModsButton = new Button(x, y, width, height, new TranslationTextComponent("modmenu.title"), button -> screen.getMinecraft().openScreen(new ModsScreen(screen)));
                event.removeWidget(widget);
                event.addWidget(newModsButton);
            }
        }
    }
}
