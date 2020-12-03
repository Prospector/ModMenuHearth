package io.github.prospector.modmenuhearth.config;

import io.github.prospector.modmenuhearth.ModMenu;
import net.minecraftforge.fml.loading.FMLLoader;

import java.io.*;

public class ModMenuConfigManager {
    private static File file;
    private static ModMenuConfig config;

    private static void prepareConfigFile() {
        if (file != null) {
            return;
        }
        file = new File(FMLLoader.getGamePath().resolve("config").toFile(), ModMenu.MOD_ID + ".json");
    }

    public static ModMenuConfig initializeConfig() {
        if (config != null) {
            return config;
        }

        config = new ModMenuConfig();
        load();

        return config;
    }

    private static void load() {
        prepareConfigFile();

        try {
            if (!file.exists()) {
                save();
            }
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));

                ModMenuConfig parsed = ModMenu.GSON.fromJson(br, ModMenuConfig.class);
                if (parsed != null) {
                    config = parsed;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load Mod Menu configuration file; reverting to defaults");
            e.printStackTrace();
        }
    }

    public static void save() {
        prepareConfigFile();

        String jsonString = ModMenu.GSON.toJson(config);

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(jsonString);
        } catch (IOException e) {
            System.err.println("Couldn't save Mod Menu configuration file");
            e.printStackTrace();
        }
    }

    public static ModMenuConfig getConfig() {
        if (config == null) {
            config = new ModMenuConfig();
        }
        return config;
    }
}
