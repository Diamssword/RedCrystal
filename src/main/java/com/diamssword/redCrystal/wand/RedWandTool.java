package com.diamssword.redCrystal.wand;

import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.storage.GlobalGlyphSettings;
import com.diamssword.redCrystal.storage.RedElementState;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class RedWandTool {
	public static final BuilderCodec<RedWandTool> CODEC = BuilderCodec.builder(RedWandTool.class, RedWandTool::new)
			.append(
					new KeyedCodec<>("Glyph", Codec.STRING),
					(meta, s) -> meta.selectedGlyph = s,
					meta -> meta.selectedGlyph
			)
			.add()
			.append(new KeyedCodec<>("GlobalSettings", GlobalGlyphSettings.CODEC), (a, b) -> a.mainSettings = b, a -> a.mainSettings)
			.add()
			.build();
	private String selectedGlyph;
	private GlobalGlyphSettings mainSettings = new GlobalGlyphSettings();

	public RedWandTool() {

	}

	public String getSelectedGlyph() {
		return selectedGlyph;
	}

	public void setSelectedGlyph(String selectedGlyph) {
		this.selectedGlyph = selectedGlyph;
	}

	public GlobalGlyphSettings getMainSettings() {
		return mainSettings;
	}

	public void setMainSettings(GlobalGlyphSettings mainSettings) {
		this.mainSettings = mainSettings;
	}

	public static RedWandTool getForStack(ItemStack stack) {
		return stack.getFromMetadataOrDefault("RedCrystalToolSettings", RedWandTool.CODEC);
	}

	public static void updateToolStack(Player player, int slot, RedWandTool settings) {
		var item = player.getInventory().getHotbar().getItemStack((short) slot);
		if(item != null) {
			player.getInventory().getHotbar().replaceItemStackInSlot((short) slot, item, item.withMetadata("RedCrystalToolSettings", RedWandTool.CODEC, settings));
		}

	}

	public static boolean createRune(ItemStack toolStack, RedElementState state, BlockFace face) {
		var tool = getForStack(toolStack);
		if(tool.getSelectedGlyph() != null) {
			var element = state.getElement(face);
			if(element == null) {
				return state.createElement(face, tool.getSelectedGlyph(), tool.mainSettings);

			}
		}
		return false;
	}

	public static Holder<EntityStore> dropDust(ComponentAccessor<EntityStore> accessor, int count, Vector3i blockPos, BlockFace face) {

		var vec = RedComponentDisplayUtils.getCenteredPosition(blockPos, face, new Vector2d(0, 0));
		return ItemComponent.generateItemDrop(accessor, new ItemStack("RedCrystal_Red_Sliver", count), vec, new Vector3f(), 0, 0, 0);
	}
}
