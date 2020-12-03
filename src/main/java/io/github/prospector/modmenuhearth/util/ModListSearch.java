package io.github.prospector.modmenuhearth.util;

import io.github.prospector.modmenuhearth.ModMenu;
import io.github.prospector.modmenuhearth.gui.ModsScreen;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class ModListSearch {

    public static boolean validSearchQuery(String query) {
        return query != null && !query.isEmpty();
    }

    public static List<ModInfo> search(ModsScreen screen, String query, List<ModInfo> candidates) {
        if (!validSearchQuery(query)) {
            return candidates;
        }
        return candidates.stream()
                .filter(modContainer -> passesFilters(screen, modContainer, query.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    private static boolean passesFilters(ModsScreen screen, ModInfo metadata, String query) {
        String modId = metadata.getModId();


        //Some basic search, could do with something more advanced but this will do for now
        if (HardcodedUtil.formatFabricModuleName(metadata.getDisplayName()).asString().toLowerCase(Locale.ROOT).contains(query) //Search mod name
                || modId.toLowerCase(Locale.ROOT).contains(query) // Search mod name
                || authorMatches(metadata, query) //Search via author
                || (ModMenu.LIBRARY_MODS.contains(modId) && "api library".contains(query)) //Search for lib mods
                || ("clientside".contains(query) && ModMenu.CLIENTSIDE_MODS.contains(modId)) //Search for clientside mods
                || ("configurations configs configures configurable".contains(query) && ModMenu.hasConfigScreenFactory(modId)) //Search for mods that can be configured
        ) {
            return true;
        }

        //Allow parent to pass filter if a child passes

        if (ModMenu.PARENT_MAP.keySet().contains(metadata)) {
            for (ModInfo child : ModMenu.PARENT_MAP.get(metadata)) {
                if (passesFilters(screen, child, query)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean authorMatches(ModInfo modContainer, String query) {
        final boolean[] matches = new boolean[1];
        modContainer.getConfigElement("authors").ifPresent((authors) -> matches[0] = Arrays.stream(((String) authors).split(",")).filter(Objects::nonNull)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .anyMatch(s -> s.contains(query.toLowerCase(Locale.ROOT))));
        return matches[0];
    }

}
