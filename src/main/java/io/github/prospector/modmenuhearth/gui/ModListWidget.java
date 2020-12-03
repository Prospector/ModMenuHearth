package io.github.prospector.modmenuhearth.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.prospector.modmenuhearth.ModMenu;
import io.github.prospector.modmenuhearth.config.ModMenuConfigManager;
import io.github.prospector.modmenuhearth.gui.entries.ChildEntry;
import io.github.prospector.modmenuhearth.gui.entries.IndependentEntry;
import io.github.prospector.modmenuhearth.gui.entries.ParentEntry;
import io.github.prospector.modmenuhearth.util.ModListSearch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.*;

public class ModListWidget extends ExtendedList<ModListEntry> implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final boolean DEBUG = Boolean.getBoolean("modmenu.debug");

	private final Map<Path, DynamicTexture> modIconsCache = new HashMap<>();
	private final ModsScreen parent;
	private List<ModInfo> modContainerList = null;
	private Set<ModInfo> addedMods = new HashSet<>();
	private String selectedModId = null;
	private boolean scrolling;

	public ModListWidget(Minecraft client, int width, int height, int y1, int y2, int entryHeight, String searchTerm, ModListWidget list, ModsScreen parent) {
		super(client, width, height, y1, y2, entryHeight);
		this.parent = parent;
		if (list != null) {
			this.modContainerList = list.modContainerList;
		}
		this.filter(searchTerm, false);
		setScrollAmount(parent.getScrollPercent() * Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
	}

	@Override
	public void setScrollAmount(double amount) {
		super.setScrollAmount(amount);
		int denominator = Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
		if (denominator <= 0) {
			parent.updateScrollPercent(0);
		} else {
			parent.updateScrollPercent(getScrollAmount() / Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
		}
	}

	@Override
	protected boolean isFocused() {
		return parent.getFocused() == this;
	}

	public void select(ModListEntry entry) {
		this.setSelected(entry);
		if (entry != null) {
			ModInfo metadata = entry.getMetadata();
			// TODO: FIX
			//			NarratorManager.INSTANCE.narrate(new TranslationTextComponent("narrator.select", HardcodedUtil.formatFabricModuleName(metadata.getName())).getString());
		}
	}

	@Override
	public void setSelected(ModListEntry entry) {
		super.setSelected(entry);
		selectedModId = entry.getMetadata().getModId();
		parent.updateSelectedEntry(getSelected());
	}

	@Override
	protected boolean isSelectedItem(int index) {
		ModListEntry selected = getSelected();
		return selected != null && selected.getMetadata().getModId().equals(getEntry(index).getMetadata().getModId());
	}

	@Override
	public int addEntry(ModListEntry entry) {
		if (addedMods.contains(entry.metadata)) {
			return 0;
		}
		addedMods.add(entry.metadata);
		int i = super.addEntry(entry);
		if (entry.getMetadata().getModId().equals(selectedModId)) {
			setSelected(entry);
		}
		return i;
	}

	@Override
	protected boolean removeEntry(ModListEntry entry) {
		addedMods.remove(entry.metadata);
		return super.removeEntry(entry);
	}

	@Override
	protected ModListEntry remove(int index) {
		addedMods.remove(getEntry(index).metadata);
		return super.remove(index);
	}

	public void reloadFilters() {
		filter(parent.getSearchInput(), true, false);
	}


	public void filter(String searchTerm, boolean refresh) {
		filter(searchTerm, refresh, true);
	}

	private void filter(String searchTerm, boolean refresh, boolean search) {
		this.clearEntries();
		addedMods.clear();
		Collection<ModInfo> mods = ModList.get().getMods();

		if (DEBUG) {
			mods = new ArrayList<>(mods);
//			mods.addAll(TestModContainer.getTestModContainers());
		}

		if (this.modContainerList == null || refresh) {
			this.modContainerList = new ArrayList<>();
			modContainerList.addAll(mods);
			this.modContainerList.sort(ModMenuConfigManager.getConfig().getSorting().getComparator());
		}

		boolean validSearch = ModListSearch.validSearchQuery(searchTerm);
		List<ModInfo> matched = ModListSearch.search(parent, searchTerm, modContainerList);

		for (ModInfo metadata : matched) {
			String modId = metadata.getModId();
			boolean library = ModMenu.LIBRARY_MODS.contains(modId);

			//Hide parent lib mods when the config is set to hide
			if (library && !ModMenuConfigManager.getConfig().showLibraries()) {
				continue;
			}

			if (!ModMenu.PARENT_MAP.values().contains(metadata)) {
				if (ModMenu.PARENT_MAP.keySet().contains(metadata)) {
					//Add parent mods when not searching
					List<ModInfo> children = ModMenu.PARENT_MAP.get(metadata);
					children.sort(ModMenuConfigManager.getConfig().getSorting().getComparator());
					ParentEntry parent = new ParentEntry(metadata, children, this);
					this.addEntry(parent);
					//Add children if they are meant to be shown
					if (this.parent.showModChildren.contains(modId)) {
						List<ModInfo> validChildren = ModListSearch.search(this.parent, searchTerm, children);
						for (ModInfo child : validChildren) {
							this.addEntry(new ChildEntry(child, parent, this, validChildren.indexOf(child) == validChildren.size() - 1));
						}
					}
				} else {
					//A mod with no children
					this.addEntry(new IndependentEntry(metadata, this));
				}
			}
		}

		if (parent.getSelectedEntry() != null && !children().isEmpty() || this.getSelected() != null && getSelected().getMetadata() != parent.getSelectedEntry().getMetadata()) {
			for (ModListEntry entry : children()) {
				if (entry.getMetadata().equals(parent.getSelectedEntry().getMetadata())) {
					setSelected(entry);
				}
			}
		} else {
			if (getSelected() == null && !children().isEmpty() && getEntry(0) != null) {
				setSelected(getEntry(0));
			}
		}

		if (getScrollAmount() > Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4))) {
			setScrollAmount(Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
		}
	}


	@Override
	protected void renderList(MatrixStack matrices, int x, int y, int mouseX, int mouseY, float delta) {
		int itemCount = this.getItemCount();
		Tessellator tessellator_1 = Tessellator.getInstance();
		BufferBuilder buffer = tessellator_1.getBuffer();

		for (int index = 0; index < itemCount; ++index) {
			int entryTop = this.getRowTop(index) + 2;
			int entryBottom = this.getRowTop(index) + this.itemHeight;
			if (entryBottom >= this.top && entryTop <= this.bottom) {
				int entryHeight = this.itemHeight - 4;
				ModListEntry entry = this.getEntry(index);
				int rowWidth = this.getRowWidth();
				int entryLeft;
				if (this.isSelectedItem(index)) {
					entryLeft = getRowLeft() - 2 + entry.getXOffset();
					int selectionRight = x + rowWidth + 2;
					RenderSystem.disableTexture();
					float float_2 = this.isFocused() ? 1.0F : 0.5F;
					RenderSystem.color4f(float_2, float_2, float_2, 1.0F);
					Matrix4f matrix = matrices.peek().getModel();
					buffer.begin(7, DefaultVertexFormats.POSITION);
					buffer.vertex(matrix, entryLeft, entryTop + entryHeight + 2, 0.0F).next();
					buffer.vertex(matrix, selectionRight, entryTop + entryHeight + 2, 0.0F).next();
					buffer.vertex(matrix, selectionRight, entryTop - 2, 0.0F).next();
					buffer.vertex(matrix, entryLeft, entryTop - 2, 0.0F).next();
					tessellator_1.draw();
					RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
					buffer.begin(7, DefaultVertexFormats.POSITION);
					buffer.vertex(matrix, entryLeft + 1, entryTop + entryHeight + 1, 0.0F).next();
					buffer.vertex(matrix, selectionRight - 1, entryTop + entryHeight + 1, 0.0F).next();
					buffer.vertex(matrix, selectionRight - 1, entryTop - 1, 0.0F).next();
					buffer.vertex(matrix, entryLeft + 1, entryTop - 1, 0.0F).next();
					tessellator_1.draw();
					RenderSystem.enableTexture();
				}

				entryLeft = this.getRowLeft();
				entry.render(matrices, index, entryTop, entryLeft, rowWidth, entryHeight, mouseX, mouseY, this.isMouseOver(mouseX, mouseY) && Objects.equals(this.getEntryAtPos(mouseX, mouseY), entry), delta);
			}
		}

	}

	@Override
	protected void updateScrollingState(double double_1, double double_2, int int_1) {
		super.updateScrollingState(double_1, double_2, int_1);
		this.scrolling = int_1 == 0 && double_1 >= (double) this.getScrollbarPositionX() && double_1 < (double) (this.getScrollbarPositionX() + 6);
	}

	@Override
	public boolean mouseClicked(double double_1, double double_2, int int_1) {
		this.updateScrollingState(double_1, double_2, int_1);
		if (!this.isMouseOver(double_1, double_2)) {
			return false;
		} else {
			ModListEntry entry = this.getEntryAtPos(double_1, double_2);
			if (entry != null) {
				if (entry.mouseClicked(double_1, double_2, int_1)) {
					this.setFocused(entry);
					this.setDragging(true);
					return true;
				}
			} else if (int_1 == 0) {
				this.clickedHeader((int) (double_1 - (double) (this.left + this.width / 2 - this.getRowWidth() / 2)), (int) (double_2 - (double) this.top) + (int) this.getScrollAmount() - 4);
				return true;
			}

			return this.scrolling;
		}
	}

	public final ModListEntry getEntryAtPos(double x, double y) {
		int int_5 = MathHelper.floor(y - (double) this.top) - this.headerHeight + (int) this.getScrollAmount() - 4;
		int index = int_5 / this.itemHeight;
		return x < (double) this.getScrollbarPositionX() && x >= (double) getRowLeft() && x <= (double) (getRowLeft() + getRowWidth()) && index >= 0 && int_5 >= 0 && index < this.getItemCount() ? this.children().get(index) : null;
	}

	@Override
	protected int getScrollbarPositionX() {
		return this.width - 6;
	}

	@Override
	public int getRowWidth() {
		return this.width - (Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)) > 0 ? 18 : 12);
	}

	@Override
	public int getRowLeft() {
		return left + 6;
	}

	public int getWidth() {
		return width;
	}

	public int getTop() {
		return this.top;
	}

	public ModsScreen getParent() {
		return parent;
	}

	@Override
	protected int getMaxPosition() {
		return super.getMaxPosition() + 4;
	}

	public int getDisplayedCount() {
		return children().size();
	}

	public int getDisplayedCountFor(Set<String> set) {
		int count = 0;
		for (ModListEntry c : children()) {
			if (set.contains(c.getMetadata().getModId())) {
				count++;
			}
		}
		return count;
	}

	@Override
	public void close() {
		for (DynamicTexture tex : this.modIconsCache.values()) {
			tex.close();
		}
	}

	DynamicTexture getCachedModIcon(Path path) {
		return this.modIconsCache.get(path);
	}

	void cacheModIcon(Path path, DynamicTexture tex) {
		this.modIconsCache.put(path, tex);
	}

	public Set<ModInfo> getCurrentModSet() {
		return addedMods;
	}
}
