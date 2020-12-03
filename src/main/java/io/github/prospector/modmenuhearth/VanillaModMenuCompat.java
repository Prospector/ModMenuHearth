package io.github.prospector.modmenuhearth;

import com.google.common.collect.ImmutableMap;
import io.github.prospector.modmenuhearth.api.ConfigScreenFactory;
import io.github.prospector.modmenuhearth.api.ModMenuApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.OptionsScreen;

import java.util.Map;

public class VanillaModMenuCompat implements ModMenuApi {
    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return ImmutableMap.of("minecraft", parent -> new OptionsScreen(parent, Minecraft.getInstance().options));
    }
}
