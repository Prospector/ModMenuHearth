package io.github.prospector.modmenuhearth.gui;

import com.google.common.base.Joiner;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.prospector.modmenuhearth.ModMenu;
import io.github.prospector.modmenuhearth.config.ModMenuConfigManager;
import io.github.prospector.modmenuhearth.util.BadgeRenderer;
import io.github.prospector.modmenuhearth.util.HardcodedUtil;
import io.github.prospector.modmenuhearth.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ModsScreen extends Screen {
	private static final ResourceLocation FILTERS_BUTTON_LOCATION = new ResourceLocation(ModMenu.MOD_ID, "textures/gui/filters_button.png");
	private static final ResourceLocation CONFIGURE_BUTTON_LOCATION = new ResourceLocation(ModMenu.MOD_ID, "textures/gui/configure_button.png");

	private static final TranslationTextComponent TOGGLE_FILTER_OPTIONS = new TranslationTextComponent("modmenu.toggleFilterOptions");
	private static final TranslationTextComponent CONFIGURE = new TranslationTextComponent("modmenu.configure");

	private static final Logger LOGGER = LogManager.getLogger();

	private TextFieldWidget searchBox;
	private DescriptionListWidget descriptionListWidget;
	private final Screen previousScreen;
	private ModListWidget modList;
	private TextComponent tooltip;
	private ModListEntry selected;
	private BadgeRenderer badgeRenderer;
	private double scrollPercent = 0;
	private boolean init = false;
	private boolean filterOptionsShown = false;
	private int paneY;
	private int paneWidth;
	private int rightPaneX;
	private int searchBoxX;
	private int filtersX;
	private int filtersWidth;
	private int searchRowWidth;
	public final Set<String> showModChildren = new HashSet<>();

	public ModsScreen(Screen previousScreen) {
		super(new TranslationTextComponent("modmenu.title"));
		this.previousScreen = previousScreen;
	}

	@Override
	public boolean mouseScrolled(double double_1, double double_2, double double_3) {
		if (modList.isMouseOver(double_1, double_2)) {
			return this.modList.mouseScrolled(double_1, double_2, double_3);
		}
		if (descriptionListWidget.isMouseOver(double_1, double_2)) {
			return this.descriptionListWidget.mouseScrolled(double_1, double_2, double_3);
		}
		return false;
	}

	@Override
	public void tick() {
		this.searchBox.tick();
	}

	@Override
	protected void init() {
		Objects.requireNonNull(this.client).keyboard.setRepeatEvents(true);
		paneY = 48;
		paneWidth = this.width / 2 - 8;
		rightPaneX = width - paneWidth;

		int searchBoxWidth = paneWidth - 32 - 22;
		searchBoxX = paneWidth / 2 - searchBoxWidth / 2 - 22 / 2;
		this.searchBox = new TextFieldWidget(this.textRenderer, searchBoxX, 22, searchBoxWidth, 20, this.searchBox, new TranslationTextComponent("modmenu.search"));
		this.searchBox.setChangedListener((string_1) -> this.modList.filter(string_1, false));
		this.modList = new ModListWidget(this.client, paneWidth, this.height, paneY + 19, this.height - 36, 36, this.searchBox.getText(), this.modList, this);
		this.modList.setLeftPos(0);
		this.descriptionListWidget = new DescriptionListWidget(this.client, paneWidth, this.height, paneY + 60, this.height - 36, textRenderer.fontHeight + 1, this);
		this.descriptionListWidget.setLeftPos(rightPaneX);
		Button configureButton = new ModMenuTexturedButtonWidget(width - 24, paneY, 20, 20, 0, 0, CONFIGURE_BUTTON_LOCATION, 32, 64, button -> {
			final String modid = Objects.requireNonNull(selected).getMetadata().getModId();
			final Screen screen = ModMenu.getConfigScreen(modid, this);
			if (screen != null) {
				client.openScreen(screen);
			} else {
				ModMenu.openConfigScreen(modid);
			}
		},
				CONFIGURE, (buttonWidget, matrices, mouseX, mouseY) -> {
			ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
			if (button.isJustHovered()) {
				this.renderTooltip(matrices, CONFIGURE, mouseX, mouseY);
			} else if (button.isFocusedButNotHovered()) {
				this.renderTooltip(matrices, CONFIGURE, button.x, button.y);
			}
		}) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				if (selected != null) {
					String modid = selected.getMetadata().getModId();
					active = ModMenu.hasConfigScreenFactory(modid) || ModMenu.hasLegacyConfigScreenTask(modid);
				} else {
					active = false;
				}
				visible = active;
				super.render(matrices, mouseX, mouseY, delta);
			}

			@Override
			public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				RenderSystem.color4f(1, 1, 1, 1f);
				super.renderButton(matrices, mouseX, mouseY, delta);
			}
		};
		int urlButtonWidths = paneWidth / 2 - 2;
		int cappedButtonWidth = Math.min(urlButtonWidths, 200);
		Button websiteButton = new Button(rightPaneX + (urlButtonWidths / 2) - (cappedButtonWidth / 2), paneY + 36, Math.min(urlButtonWidths, 200), 20,
				new TranslationTextComponent("modmenu.website"), button -> {
			final ModInfo metadata = Objects.requireNonNull(selected).getMetadata();
			this.client.openScreen(new ConfirmOpenLinkScreen((bool) -> {
				if (bool) {
//					Util.getOperatingSystem().open(metadata.getContact().get("homepage").get());
				}
				this.client.openScreen(this);
			}, /*metadata.getContact().get("homepage").get()*/ "", true));
		}) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				visible = selected != null;
				active = visible && /*selected.getMetadata().getContact().get("homepage").isPresent()*/ false;
				super.render(matrices, mouseX, mouseY, delta);
			}
		};
		Button issuesButton = new Button(rightPaneX + urlButtonWidths + 4 + (urlButtonWidths / 2) - (cappedButtonWidth / 2), paneY + 36, Math.min(urlButtonWidths, 200), 20,
				new TranslationTextComponent("modmenu.issues"), button -> {
			final ModInfo metadata = Objects.requireNonNull(selected).getMetadata();
			this.client.openScreen(new ConfirmOpenLinkScreen((bool) -> {
				if (bool) {
//					Util.getOperatingSystem().open(metadata.getContact().get("issues").get());
				}
				this.client.openScreen(this);
			}, /*metadata.getContact().get("issues").get()*/ "", true));
		}) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				visible = selected != null;
				active = visible && /*selected.getMetadata().getContact().get("issues").isPresent()*/ false;
				super.render(matrices, mouseX, mouseY, delta);
			}
		};
		this.children.add(this.searchBox);
		this.addButton(new ModMenuTexturedButtonWidget(paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 2, 22, 20, 20, 0, 0, FILTERS_BUTTON_LOCATION, 32, 64, button -> filterOptionsShown = !filterOptionsShown, TOGGLE_FILTER_OPTIONS, (buttonWidget, matrices, mouseX, mouseY) -> {
			ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
			if (button.isJustHovered()) {
				this.renderTooltip(matrices, TOGGLE_FILTER_OPTIONS, mouseX, mouseY);
			} else if (button.isFocusedButNotHovered()) {
				this.renderTooltip(matrices, TOGGLE_FILTER_OPTIONS, button.x, button.y);
			}
		}));
		TextComponent showLibrariesText = new TranslationTextComponent("modmenu.showLibraries", new TranslationTextComponent("modmenu.showLibraries." + ModMenuConfigManager.getConfig().showLibraries()));
		TextComponent sortingText = new TranslationTextComponent("modmenu.sorting", new TranslationTextComponent(ModMenuConfigManager.getConfig().getSorting().getTranslationKey()));
		int showLibrariesWidth = textRenderer.getWidth(showLibrariesText) + 20;
		int sortingWidth = textRenderer.getWidth(sortingText) + 20;
		filtersWidth = showLibrariesWidth + sortingWidth + 2;
		searchRowWidth = searchBoxX + searchBoxWidth + 22;
		updateFiltersX();
		this.addButton(new Button(filtersX, 45, sortingWidth, 20, sortingText, button -> {
			ModMenuConfigManager.getConfig().toggleSortMode();
			modList.reloadFilters();
		}) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				matrices.translate(0, 0, 1);
				visible = filterOptionsShown;
				this.setMessage(new TranslationTextComponent("modmenu.sorting", new TranslationTextComponent(ModMenuConfigManager.getConfig().getSorting().getTranslationKey())));
				super.render(matrices, mouseX, mouseY, delta);
			}
		});
		this.addButton(new Button(filtersX + sortingWidth + 2, 45, showLibrariesWidth, 20, new TranslationTextComponent("modmenu.showLibraries", new TranslationTextComponent("modmenu.showLibraries." + ModMenuConfigManager.getConfig().showLibraries())), button -> {
			ModMenuConfigManager.getConfig().toggleShowLibraries();
			modList.reloadFilters();
		}) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				matrices.translate(0, 0, 1);
				visible = filterOptionsShown;
				this.setMessage(new TranslationTextComponent("modmenu.showLibraries", new TranslationTextComponent("modmenu.showLibraries." + ModMenuConfigManager.getConfig().showLibraries())));
				super.render(matrices, mouseX, mouseY, delta);
			}
		});
		this.children.add(this.modList);
		if (!ModMenuConfigManager.getConfig().isHidingConfigurationButtons()) {
			this.addButton(configureButton);
		}
		this.addButton(websiteButton);
		this.addButton(issuesButton);
		this.children.add(this.descriptionListWidget);
		this.addButton(new Button(this.width / 2 - 154, this.height - 28, 150, 20, new TranslationTextComponent("modmenu.modsFolder"), button -> Util.getOperatingSystem().open(new File(FMLLoader.getGamePath().toFile(), "mods"))));
		this.addButton(new Button(this.width / 2 + 4, this.height - 28, 150, 20, DialogTexts.DONE, button -> client.openScreen(previousScreen)));
		this.setInitialFocus(this.searchBox);

		init = true;
	}

	@Override
	public boolean keyPressed(int int_1, int int_2, int int_3) {
		return super.keyPressed(int_1, int_2, int_3) || this.searchBox.keyPressed(int_1, int_2, int_3);
	}

	@Override
	public boolean charTyped(char char_1, int int_1) {
		return this.searchBox.charTyped(char_1, int_1);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.tooltip = null;
		ModListEntry selectedEntry = selected;
		if (selectedEntry != null) {
			this.descriptionListWidget.render(matrices, mouseX, mouseY, delta);
		}
		this.modList.render(matrices, mouseX, mouseY, delta);
		this.searchBox.render(matrices, mouseX, mouseY, delta);
		RenderSystem.disableBlend();
		this.drawTextWithShadow(matrices, this.textRenderer, this.title, this.modList.getWidth() / 2, 8, 16777215);
		TextComponent fullModCount = computeModCountText(true);
		if (updateFiltersX()) {
			if (filterOptionsShown) {
				if (!ModMenuConfigManager.getConfig().showLibraries() || textRenderer.getWidth(fullModCount) <= filtersX - 5) {
					textRenderer.draw(matrices, fullModCount.asOrderedText(), searchBoxX, 52, 0xFFFFFF);
				} else {
					textRenderer.draw(matrices, computeModCountText(false).asOrderedText(), searchBoxX, 46, 0xFFFFFF);
					textRenderer.draw(matrices, computeLibraryCountText().asOrderedText(), searchBoxX, 57, 0xFFFFFF);
				}
			} else {
				if (!ModMenuConfigManager.getConfig().showLibraries() || textRenderer.getWidth(fullModCount) <= modList.getWidth() - 5) {
					textRenderer.draw(matrices, fullModCount.asOrderedText(), searchBoxX, 52, 0xFFFFFF);
				} else {
					textRenderer.draw(matrices, computeModCountText(false).asOrderedText(), searchBoxX, 46, 0xFFFFFF);
					textRenderer.draw(matrices, computeLibraryCountText().asOrderedText(), searchBoxX, 57, 0xFFFFFF);
				}
			}
		}
		if (selectedEntry != null) {
			ModInfo metadata = selectedEntry.getMetadata();
			int x = rightPaneX;
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.selected.bindIconTexture();
			RenderSystem.enableBlend();
			drawTexture(matrices, x, paneY, 0.0F, 0.0F, 32, 32, 32, 32);
			RenderSystem.disableBlend();
			int lineSpacing = textRenderer.fontHeight + 1;
			int imageOffset = 36;
			TextComponent name = new StringTextComponent(metadata.getDisplayName());
			name = HardcodedUtil.formatFabricModuleName(name.asString());
			ITextProperties trimmedName = name;
			int maxNameWidth = this.width - (x + imageOffset);
			if (textRenderer.getWidth(name) > maxNameWidth) {
				ITextProperties ellipsis = ITextProperties.plain("...");
				trimmedName = ITextProperties.concat(textRenderer.trimToWidth(name, maxNameWidth - textRenderer.getWidth(ellipsis)), ellipsis);
			}
			textRenderer.draw(matrices, LanguageMap.getInstance().reorder(trimmedName), x + imageOffset, paneY + 1, 0xFFFFFF);
			if (mouseX > x + imageOffset && mouseY > paneY + 1 && mouseY < paneY + 1 + textRenderer.fontHeight && mouseX < x + imageOffset + textRenderer.getWidth(trimmedName)) {
				setTooltip(new TranslationTextComponent("modmenu.modIdToolTip", metadata.getModId()));
			}
			if (init || badgeRenderer == null || badgeRenderer.getMetadata() != metadata) {
				badgeRenderer = new BadgeRenderer(x + imageOffset + Objects.requireNonNull(this.client).textRenderer.getWidth(trimmedName) + 2, paneY, width - 28, selectedEntry.metadata, this);
				init = false;
			}
			badgeRenderer.draw(matrices, mouseX, mouseY);
			textRenderer.draw(matrices, "v" + metadata.getVersion(), x + imageOffset, paneY + 2 + lineSpacing, 0x808080);
			String authors;
			List<String> names = new ArrayList<>();

			/*metadata.getAuthors().stream()
					.filter(Objects::nonNull)
					.map(Person::getName)
					.filter(Objects::nonNull)
					.forEach(names::add);*/

			if (!names.isEmpty()) {
				if (names.size() > 1) {
					authors = Joiner.on(", ").join(names);
				} else {
					authors = names.get(0);
				}
				RenderUtils.drawWrappedString(matrices, I18n.translate("modmenu.authorPrefix", authors), x + imageOffset, paneY + 2 + lineSpacing * 2, paneWidth - imageOffset - 4, 1, 0x808080);
			}
		}
		super.render(matrices, mouseX, mouseY, delta);
		if (this.tooltip != null) {
			this.renderOrderedTooltip(matrices, textRenderer.wrapLines(this.tooltip, Integer.MAX_VALUE), mouseX, mouseY);
		}
	}

	private TextComponent computeModCountText(boolean includeLibs) {
		int[] rootMods = formatModCount(ModMenu.ROOT_NONLIB_MODS);

		if (includeLibs && ModMenuConfigManager.getConfig().showLibraries()) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_LIBRARIES);
			return translateNumeric("modmenu.showingModsLibraries", rootMods, rootLibs);
		} else {
			return translateNumeric("modmenu.showingMods", rootMods);
		}
	}

	private TextComponent computeLibraryCountText() {
		if (ModMenuConfigManager.getConfig().showLibraries()) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_LIBRARIES);
			return translateNumeric("modmenu.showingLibraries", rootLibs);
		} else {
			return new StringTextComponent(null);
		}
	}

	private static TextComponent translateNumeric(String key, int[]... args) {
		Object[] realArgs = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			NumberFormat nf = NumberFormat.getInstance();
			if (args[i].length == 1) {
				realArgs[i] = nf.format(args[i][0]);
			} else {
				assert args[i].length == 2;
				realArgs[i] = nf.format(args[i][0]) + "/" + nf.format(args[i][1]);
			}
		}

		int[] override = new int[args.length];
		Arrays.fill(override, -1);
		for (int i = 0; i < args.length; i++) {
			int[] arg = args[i];
			if (arg == null) {
				throw new NullPointerException("args[" + i + "]");
			}
			if (arg.length == 1) {
				override[i] = arg[0];
			}
		}

		String lastKey = key;
		for (int flags = (1 << args.length) - 1; flags >= 0; flags--) {
			StringBuilder fullKey = new StringBuilder(key);
			for (int i = 0; i < args.length; i++) {
				fullKey.append('.');
				if (((flags & (1 << i)) != 0) && override[i] != -1) {
					fullKey.append(override[i]);
				} else {
					fullKey.append('a');
				}
			}
			lastKey = fullKey.toString();
			if (I18n.hasTranslation(lastKey)) {
//				return lastKey + Arrays.toString(realArgs);
				return new TranslationTextComponent(lastKey, realArgs);
			}
		}
//		return lastKey + Arrays.toString(realArgs);
		return new TranslationTextComponent(lastKey, realArgs);
	}

	private int[] formatModCount(Set<String> set) {
		int visible = modList.getDisplayedCountFor(set);
		int total = set.size();
		if (visible == total) {
			return new int[]{total};
		}
		return new int[]{visible, total};
	}

	@Override
	public void renderBackground(MatrixStack matrices) {
		ModsScreen.overlayBackground(0, 0, this.width, this.height, 64, 64, 64, 255, 255);
	}

	static void overlayBackground(int x1, int y1, int x2, int y2, int red, int green, int blue, int startAlpha, int endAlpha) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		Objects.requireNonNull(Minecraft.getInstance()).getTextureManager().bindTexture(AbstractGui.OPTIONS_BACKGROUND_TEXTURE);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		buffer.begin(7, DefaultVertexFormats.POSITION_TEXTURE_COLOR);
		buffer.vertex(x1, y2, 0.0D).texture(x1 / 32.0F, y2 / 32.0F).color(red, green, blue, endAlpha).next();
		buffer.vertex(x2, y2, 0.0D).texture(x2 / 32.0F, y2 / 32.0F).color(red, green, blue, endAlpha).next();
		buffer.vertex(x2, y1, 0.0D).texture(x2 / 32.0F, y1 / 32.0F).color(red, green, blue, startAlpha).next();
		buffer.vertex(x1, y1, 0.0D).texture(x1 / 32.0F, y1 / 32.0F).color(red, green, blue, startAlpha).next();
		tessellator.draw();
	}

	@Override
	public void onClose() {
		super.onClose();
		this.modList.close();
		this.client.openScreen(this.previousScreen);
	}

	private void setTooltip(TextComponent tooltip) {
		this.tooltip = tooltip;
	}

	ModListEntry getSelectedEntry() {
		return selected;
	}

	void updateSelectedEntry(ModListEntry entry) {
		if (entry != null) {
			this.selected = entry;
		}
	}

	double getScrollPercent() {
		return scrollPercent;
	}

	void updateScrollPercent(double scrollPercent) {
		this.scrollPercent = scrollPercent;
	}

	public String getSearchInput() {
		return searchBox.getText();
	}

	private boolean updateFiltersX() {
		if ((filtersWidth + textRenderer.getWidth(computeModCountText(true)) + 20) >= searchRowWidth && ((filtersWidth + textRenderer.getWidth(computeModCountText(false)) + 20) >= searchRowWidth || (filtersWidth + textRenderer.getWidth(computeLibraryCountText()) + 20) >= searchRowWidth)) {
			filtersX = paneWidth / 2 - filtersWidth / 2;
			return !filterOptionsShown;
		} else {
			filtersX = searchRowWidth - filtersWidth + 1;
			return true;
		}
	}

	@Override
	public void filesDragged(List<Path> paths) {
		Path modsDirectory = FMLLoader.getGamePath().resolve("mods");

		// Filter out none mods
		List<Path> mods = paths.stream()
				.filter(ModsScreen::isFabricMod)
				.collect(Collectors.toList());

		if (mods.isEmpty()) {
			return;
		}

		String modList = mods.stream()
				.map(Path::getFileName)
				.map(Path::toString)
				.collect(Collectors.joining(", "));

		this.client.openScreen(new ConfirmScreen((value) -> {
			if (value) {
				boolean allSuccessful = true;

				for (Path path : mods) {
					try {
						Files.copy(path, modsDirectory.resolve(path.getFileName()));
					} catch (IOException e) {
						LOGGER.warn("Failed to copy mod from {} to {}", path, modsDirectory.resolve(path.getFileName()));
						SystemToast.addPackCopyFailure(client, path.toString());
						allSuccessful = false;
						break;
					}
				}

				if (allSuccessful) {
					SystemToast.add(client.getToastManager(), SystemToast.Type.TUTORIAL_HINT, new TranslationTextComponent("modmenu.dropSuccessful.line1"), new TranslationTextComponent("modmenu.dropSuccessful.line2"));
				}
			}
			this.client.openScreen(this);
		}, new TranslationTextComponent("modmenu.dropConfirm"), new StringTextComponent(modList)));
	}

	private static boolean isFabricMod(Path mod) {
		try (JarFile jarFile = new JarFile(mod.toFile())) {
			return jarFile.getEntry("fabric.mod.json") != null;
		} catch (IOException e) {
			return false;
		}
	}
}
