package io.github.prospector.modmenuhearth.gui.entries;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.prospector.modmenuhearth.gui.ModListEntry;
import io.github.prospector.modmenuhearth.gui.ModListWidget;
import net.minecraft.client.gui.AbstractGui;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

public class ChildEntry extends ModListEntry {
	private boolean bottomChild;
	private ParentEntry parent;

	public ChildEntry(ModInfo container, ParentEntry parent, ModListWidget list, boolean bottomChild) {
		super(container, list);
		this.bottomChild = bottomChild;
		this.parent = parent;
	}

	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
		super.render(matrices, index, y, x, rowWidth, rowHeight, mouseX, mouseY, isSelected, delta);
		x += 4;
		int color = 0xFFA0A0A0;
		AbstractGui.fill(matrices, x, y - 2, x + 1, y + (bottomChild ? rowHeight / 2 : rowHeight + 2), color);
		AbstractGui.fill(matrices, x, y + rowHeight / 2, x + 7, y + rowHeight / 2 + 1, color);
	}

	@Override
	public int getXOffset() {
		return 13;
	}
}
