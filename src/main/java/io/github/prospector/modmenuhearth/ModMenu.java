package io.github.prospector.modmenuhearth;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.prospector.modmenuhearth.api.ConfigScreenFactory;
import io.github.prospector.modmenuhearth.api.ModMenuApi;
import io.github.prospector.modmenuhearth.config.ModMenuConfigManager;
import io.github.prospector.modmenuhearth.util.HardcodedUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.NumberFormat;
import java.util.*;

@Mod("modmenu")
public class ModMenu {
    public static final String MOD_ID = "modmenu";
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<String, Runnable> LEGACY_CONFIG_SCREEN_TASKS = new HashMap<>();
    public static final Set<String> LIBRARY_MODS = new HashSet<>();
    public static final Set<String> ROOT_LIBRARIES = new HashSet<>();
    public static final Set<String> CHILD_LIBRARIES = new HashSet<>();
    public static final Set<String> ALL_NONLIB_MODS = new HashSet<>();
    public static final Set<String> ROOT_NONLIB_MODS = new HashSet<>();
    public static final Set<String> CHILD_NONLIB_MODS = new HashSet<>();
    public static final Set<String> CLIENTSIDE_MODS = new HashSet<>();
    public static final Set<String> DEPRECATED_MODS = new HashSet<>();
    public static final Set<String> PATCHWORK_FORGE_MODS = new HashSet<>();
    public static final LinkedListMultimap<ModInfo, ModInfo> PARENT_MAP = LinkedListMultimap.create();
    private static ImmutableMap<String, ConfigScreenFactory<?>> configScreenFactories = ImmutableMap.of();

    public static boolean hasConfigScreenFactory(String modid) {
        try {
            return configScreenFactories.containsKey(modid) && configScreenFactories.get(modid).create(Minecraft.getInstance().currentScreen) != null;
        } catch (Throwable e) {
            LOGGER.error("Caught exception from " + modid + " on ModMenu.hasConfigScreenFactory");
            e.printStackTrace();
            return false;
        }
    }

    public static Screen getConfigScreen(String modid, Screen menuScreen) {
        ConfigScreenFactory<?> factory = configScreenFactories.get(modid);
        return factory != null ? factory.create(menuScreen) : null;
    }

    public static void openConfigScreen(String modid) {
        Runnable opener = LEGACY_CONFIG_SCREEN_TASKS.get(modid);
        if (opener != null) opener.run();
    }

    public static void addLegacyConfigScreenTask(String modid, Runnable task) {
        LEGACY_CONFIG_SCREEN_TASKS.putIfAbsent(modid, task);
    }

    public static boolean hasLegacyConfigScreenTask(String modid) {
        return LEGACY_CONFIG_SCREEN_TASKS.containsKey(modid);
    }

    public static void addLibraryMod(String modid) {
        LIBRARY_MODS.add(modid);
    }

    public ModMenu() {
        ModMenuConfigManager.initializeConfig();
        /*Map<String, ConfigScreenFactory<?>> factories = new HashMap<>();
        *//*FabricLoader.getInstance().getEntrypointContainers("modmenu", ModMenuApi.class).forEach(entrypoint -> {
            ModMenuApi api = entrypoint.getEntrypoint();
            factories.put(entrypoint.getProvider().getMetadata().getId(), api.getModConfigScreenFactory());
            api.getProvidedConfigScreenFactories().forEach(factories::putIfAbsent);
        });*//*
        configScreenFactories = new ImmutableMap.Builder<String, ConfigScreenFactory<?>>().putAll(factories).build();
        Collection<ModInfo> mods = ModList.get().getMods();

        HardcodedUtil.initializeHardcodings();
        for (ModInfo mod : mods) {
            String id = mod.getModId();
            if ("minecraft".equals(id)) {
                continue;
            }*/
            /*
            boolean isLibrary = (mod.containsCustomValue("modmenu:api") && mod.getCustomValue("modmenu:api").getAsBoolean()) || (mod.containsCustomValue("fabric-loom:generated") && mod.getCustomValue("fabric-loom:generated").getAsBoolean());
            if (isLibrary) {
                addLibraryMod(id);
            }
            boolean hasClientValue = false mod.containsCustomValue("modmenu:clientsideOnly");
            boolean clientEnvironmentOnly = mod.getEnvironment() == ModEnvironment.CLIENT;
            boolean clientsideOnlyValue = hasClientValue && mod.getCustomValue("modmenu:clientsideOnly").getAsBoolean();
            if (clientEnvironmentOnly && !hasClientValue || hasClientValue && clientsideOnlyValue) {
                if (clientEnvironmentOnly && clientsideOnlyValue) {
                    LOGGER.info("Mod '" + mod.getId() + "' uses the modmenu:clientsideOnly custom value unnecessarily, as it can be inferred from the mod's declared environment.");
                }
                CLIENTSIDE_MODS.add(id);
            }
            if (mod.containsCustomValue("modmenu:deprecated") && mod.getCustomValue("modmenu:deprecated").getAsBoolean()) {
                DEPRECATED_MODS.add(id);
            }
            if (mod.containsCustomValue("patchwork:patcherMeta")) {
                PATCHWORK_FORGE_MODS.add(id);
            }
            boolean hasParent = false;
            if (mod.containsCustomValue("modmenu:parent")) {
                String parentId = mod.getCustomValue("modmenu:parent").getAsString();
                if (parentId != null) {
                    Optional<ModContainer> parent = FabricLoader.getInstance().getModContainer(parentId);
                    if (parent.isPresent()) {
                        hasParent = true;
                        PARENT_MAP.put(parent.get(), mod);
                        if (isLibrary) {
                            CHILD_LIBRARIES.add(id);
                        } else {
                            CHILD_NONLIB_MODS.add(id);
                            ALL_NONLIB_MODS.add(id);
                        }
                    }
                }
            } else {
                HardcodedUtil.hardcodeModuleMetadata(mod, mod, id);
                isLibrary = LIBRARY_MODS.contains(id);
                hasParent = PARENT_MAP.containsValue(mod);
            }

            if (isLibrary) {
                (hasParent ? CHILD_LIBRARIES : ROOT_LIBRARIES).add(id);
            } else {
                (hasParent ? CHILD_NONLIB_MODS : ROOT_NONLIB_MODS).add(id);
                ALL_NONLIB_MODS.add(id);
            }
             */
//        }
    }

    public static String getDisplayedModCount() {
        return NumberFormat.getInstance().format(ROOT_NONLIB_MODS.size());
    }
}
