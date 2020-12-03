package io.github.prospector.modmenuhearth.util;

import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import org.apache.commons.lang3.text.WordUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HardcodedUtil {
    private static final Pattern FABRIC_PATTERN = Pattern.compile("^fabric-.*(-v\\d+)$");
    private static final Set<String> FABRIC_MODS = new HashSet<>();
    private static final HashMap<String, String> HARDCODED_DESCRIPTIONS = new HashMap<>();

    public static void initializeHardcodings() {
//		FABRIC_MODS.add("fabric");
//		FABRIC_MODS.add("fabricloader");
        HARDCODED_DESCRIPTIONS.put("minecraft", "The base game.");
    }

    public static TextComponent formatFabricModuleName(String name) {
        Matcher matcher = FABRIC_PATTERN.matcher(name);
        if (matcher.matches() || name.equals("fabric-renderer-indigo") || name.equals("fabric-api-base")) {
            if (matcher.matches()) {
                String v = matcher.group(1);
                name = WordUtils.capitalize(name.replace(v, "").replace("-", " "));
                name = name + " (" + v.replace("-", "") + ")";
            } else {
                name = WordUtils.capitalize(name.replace("-", " "));
            }
            name = name.replace("Api", "API");
            name = name.replace("Blockentity", "BlockEntity");
        }
        return new StringTextComponent(name);
    }

    public static String getHardcodedDescription(String id) {
        return HARDCODED_DESCRIPTIONS.getOrDefault(id, "");
    }

    public static Set<String> getFabricMods() {
        return FABRIC_MODS;
    }

    public static HashMap<String, String> getHardcodedDescriptions() {
        return HARDCODED_DESCRIPTIONS;
    }
}
