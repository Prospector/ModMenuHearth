package io.github.prospector.modmenuhearth.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.prospector.modmenuhearth.util.BadgeRenderer;
import io.github.prospector.modmenuhearth.util.HardcodedUtil;
import io.github.prospector.modmenuhearth.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.TextComponent;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class ModListEntry extends ExtendedList.AbstractListEntry<ModListEntry> {
	public static final ResourceLocation UNKNOWN_ICON = new ResourceLocation("textures/misc/unknown_pack.png");
	private static final Logger LOGGER = LogManager.getLogger();

	protected final Minecraft client;
	protected final ModInfo metadata;
	protected final ModListWidget list;
	protected ResourceLocation iconLocation;

	public ModListEntry(ModInfo container, ModListWidget list) {
		this.list = list;
		this.metadata = container;
		this.client = Minecraft.getInstance();
	}

	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
		x += getXOffset();
		rowWidth -= getXOffset();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.bindIconTexture();
		RenderSystem.enableBlend();
		AbstractGui.drawTexture(matrices, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
		RenderSystem.disableBlend();
		TextComponent name = HardcodedUtil.formatFabricModuleName(metadata.getDisplayName());
		ITextProperties trimmedName = name;
		int maxNameWidth = rowWidth - 32 - 3;
		FontRenderer font = this.client.textRenderer;
		if (font.getWidth(name) > maxNameWidth) {
			ITextProperties ellipsis = ITextProperties.plain("...");
			trimmedName = ITextProperties.concat(font.trimToWidth(name, maxNameWidth - font.getWidth(ellipsis)), ellipsis);
		}
		font.draw(matrices, LanguageMap.getInstance().reorder(trimmedName), x + 32 + 3, y + 1, 0xFFFFFF);
		new BadgeRenderer(x + 32 + 3 + font.getWidth(name) + 2, y, x + rowWidth, metadata, list.getParent()).draw(matrices, mouseX, mouseY);
		String description = metadata.getDescription();
		if (description.isEmpty() && HardcodedUtil.getHardcodedDescriptions().containsKey(metadata.getModId())) {
			description = HardcodedUtil.getHardcodedDescription(metadata.getModId());
		}
		RenderUtils.drawWrappedString(matrices, description, (x + 32 + 3 + 4), (y + client.textRenderer.fontHeight + 2), rowWidth - 32 - 7, 2, 0x808080);
	}

	private DynamicTexture createIcon() {
		try {
			Path path = null; /*container.getPath(metadata.getIconPath(64 * MinecraftClient.getInstance().options.guiScale).orElse("assets/" + metadata.getId() + "/icon.png"))*/;
			DynamicTexture cached = this.list.getCachedModIcon(path);
			if (cached != null) {
				return cached;
			}
			if (!Files.exists(path)) {
//				ModInfo modMenu = FabricLoader.getInstance().getModContainer(ModMenu.MOD_ID).orElseThrow(IllegalAccessError::new);
//				if (HardcodedUtil.getFabricMods().contains(metadata.getId())) {
//					path = modMenu.getPath("assets/" + ModMenu.MOD_ID + "/fabric_icon.png");
//				} else if (metadata.getId().equals("minecraft")) {
//					path = modMenu.getPath("assets/" + ModMenu.MOD_ID + "/mc_icon.png");
//				} else {
//					path = modMenu.getPath("assets/" + ModMenu.MOD_ID + "/grey_fabric_icon.png");
//				}
			}
			cached = this.list.getCachedModIcon(path);
			if (cached != null) {
				return cached;
			}
			try (InputStream inputStream = Files.newInputStream(path)) {
				NativeImage image = NativeImage.read(Objects.requireNonNull(inputStream));
				Validate.validState(image.getHeight() == image.getWidth(), "Must be square icon");
				DynamicTexture tex = new DynamicTexture(image);
				this.list.cacheModIcon(path, tex);
				return tex;
			}

		} catch (Throwable t) {
			LOGGER.error("Invalid icon for mod {}", this.metadata.getDisplayName(), t);
			return null;
		}
	}

	@Override
	public boolean mouseClicked(double v, double v1, int i) {
		list.select(this);
		return true;
	}

	public ModInfo getMetadata() {
		return metadata;
	}

	public void bindIconTexture() {
		if (this.iconLocation == null) {
			this.iconLocation = new ResourceLocation("modmenu", metadata.getModId() + "_icon");
			DynamicTexture icon = this.createIcon();
			if (icon != null) {
				this.client.getTextureManager().registerTexture(this.iconLocation, icon);
			} else {
				this.iconLocation = UNKNOWN_ICON;
			}
		}
		this.client.getTextureManager().bindTexture(this.iconLocation);
	}

	public int getXOffset() {
		return 0;
	}
}
