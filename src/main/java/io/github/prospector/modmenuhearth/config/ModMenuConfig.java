package io.github.prospector.modmenuhearth.config;

import com.google.gson.annotations.SerializedName;
import io.github.prospector.modmenuhearth.util.HardcodedUtil;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.util.Comparator;

public class ModMenuConfig {
	private boolean showLibraries = false;
	private boolean hideConfigButtons = false;
	private Sorting sorting = Sorting.ASCENDING;

	public void toggleShowLibraries() {
		this.showLibraries = !this.showLibraries;
		ModMenuConfigManager.save();
	}

	public void toggleSortMode() {
		this.sorting = Sorting.values()[(getSorting().ordinal() + 1) % Sorting.values().length];
		ModMenuConfigManager.save();
	}

	public boolean showLibraries() {
		return showLibraries;
	}

	public boolean isHidingConfigurationButtons() {
		return hideConfigButtons;
	}

	public Sorting getSorting() {
		return sorting == null ? Sorting.ASCENDING : sorting;
	}

	public enum Sorting {
		@SerializedName("ascending")
		ASCENDING(Comparator.comparing(modContainer -> HardcodedUtil.formatFabricModuleName(modContainer.getDisplayName()).asString()), "modmenu.sorting.ascending"),
		@SerializedName("descending")
		DESCENDING(ASCENDING.getComparator().reversed(), "modmenu.sorting.decending");

		Comparator<ModInfo> comparator;
		String translationKey;

		Sorting(Comparator<ModInfo> comparator, String translationKey) {
			this.comparator = comparator;
			this.translationKey = translationKey;
		}

		public Comparator<ModInfo> getComparator() {
			return comparator;
		}

		public String getTranslationKey() {
			return translationKey;
		}
	}
}
