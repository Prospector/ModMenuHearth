package io.github.prospector.modmenuhearth.gui.entries;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.prospector.modmenuhearth.ModMenu;
import io.github.prospector.modmenuhearth.gui.ModListEntry;
import io.github.prospector.modmenuhearth.gui.ModListWidget;
import io.github.prospector.modmenuhearth.util.ModListSearch;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ParentEntry extends ModListEntry {
	private static final ResourceLocation PARENT_MOD_TEXTURE = new ResourceLocation(ModMenu.MOD_ID, "textures/gui/parent_mod.png");
	protected List<ModInfo> children;
	protected ModListWidget list;
	protected boolean hoveringIcon = false;

	public ParentEntry(ModInfo parent, List<ModInfo> children, ModListWidget list) {
		super(parent, list);
		this.children = children;
		this.list = list;
	}

	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
		super.render(matrices, index, y, x, rowWidth, rowHeight, mouseX, mouseY, isSelected, delta);
		FontRenderer font = client.textRenderer;
		int childrenBadgeHeight = font.fontHeight;
		int childrenBadgeWidth = font.fontHeight;
		int shownChildren = ModListSearch.search(list.getParent(), list.getParent().getSearchInput(), getChildren()).size();
		TextComponent str = shownChildren == children.size() ? new StringTextComponent(String.valueOf(shownChildren)) : new StringTextComponent(shownChildren + "/" + children.size());
		int childrenWidth = font.getWidth(str) - 1;
		if (childrenBadgeWidth < childrenWidth + 4) {
			childrenBadgeWidth = childrenWidth + 4;
		}
		int childrenBadgeX = x + 32 - childrenBadgeWidth;
		int childrenBadgeY = y + 32 - childrenBadgeHeight;
		int childrenOutlineColor = 0x8810d098;
		int childrenFillColor = 0x88046146;
		AbstractGui.fill(matrices, childrenBadgeX + 1, childrenBadgeY, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + 1, childrenOutlineColor);
		AbstractGui.fill(matrices, childrenBadgeX, childrenBadgeY + 1, childrenBadgeX + 1, childrenBadgeY + childrenBadgeHeight - 1, childrenOutlineColor);
		AbstractGui.fill(matrices, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + 1, childrenBadgeX + childrenBadgeWidth, childrenBadgeY + childrenBadgeHeight - 1, childrenOutlineColor);
		AbstractGui.fill(matrices, childrenBadgeX + 1, childrenBadgeY + 1, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + childrenBadgeHeight - 1, childrenFillColor);
		AbstractGui.fill(matrices, childrenBadgeX + 1, childrenBadgeY + childrenBadgeHeight - 1, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + childrenBadgeHeight, childrenOutlineColor);
		font.draw(matrices, str.asOrderedText(), childrenBadgeX + (float) childrenBadgeWidth / 2 - (float) childrenWidth / 2, childrenBadgeY + 1, 0xCACACA);
		this.hoveringIcon = mouseX >= x - 1 && mouseX <= x - 1 + 32 && mouseY >= y - 1 && mouseY <= y - 1 + 32;
		if (isMouseOver(mouseX, mouseY)) {
			AbstractGui.fill(matrices, x, y, x + 32, y + 32, 0xA0909090);
			this.client.getTextureManager().bindTexture(PARENT_MOD_TEXTURE);
			int xOffset = list.getParent().showModChildren.contains(getMetadata().getModId()) ? 32 : 0;
			int yOffset = hoveringIcon ? 32 : 0;
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			AbstractGui.drawTexture(matrices, x, y, xOffset, yOffset, 32 + xOffset, 32 + yOffset, 256, 256);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int i) {
		if (hoveringIcon) {
			String id = getMetadata().getModId();
			if (list.getParent().showModChildren.contains(id)) {
				list.getParent().showModChildren.remove(id);
			} else {
				list.getParent().showModChildren.add(id);
			}
			list.filter(list.getParent().getSearchInput(), false);
		}
		return super.mouseClicked(mouseX, mouseY, i);
	}

	@Override
	public boolean keyPressed(int int_1, int int_2, int int_3) {
		if (int_1 == GLFW.GLFW_KEY_ENTER) {
			String id = getMetadata().getModId();
			if (list.getParent().showModChildren.contains(id)) {
				list.getParent().showModChildren.remove(id);
			} else {
				list.getParent().showModChildren.add(id);
			}
			list.filter(list.getParent().getSearchInput(), false);
			return true;
		}
		return super.keyPressed(int_1, int_2, int_3);
	}

	public void setChildren(List<ModInfo> children) {
		this.children = children;
	}

	public void addChildren(List<ModInfo> children) {
		this.children.addAll(children);
	}

	public void addChildren(ModInfo... children) {
		this.children.addAll(Arrays.asList(children));
	}

	public List<ModInfo> getChildren() {
		return children;
	}

	@Override
	public boolean isMouseOver(double double_1, double double_2) {
		return Objects.equals(this.list.getEntryAtPos(double_1, double_2), this);
	}
}
