package com.diamssword.redCrystal.wand;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;

public class RedWandTool {
	public static final BuilderCodec<RedWandTool> CODEC = BuilderCodec.builder(RedWandTool.class, RedWandTool::new)
			.appendInherited(
					new KeyedCodec<>("Glyph", Codec.STRING),
					(meta, s) -> meta.selectedGlyph = s,
					meta -> meta.selectedGlyph,
					(meta, parent) -> meta.selectedGlyph = parent.selectedGlyph
			)
			.add()
			.build();
	private String selectedGlyph;

	public RedWandTool() {

	}

	public String getSelectedGlyph() {
		return selectedGlyph;
	}

	public void setSelectedGlyph(String selectedGlyph) {
		this.selectedGlyph = selectedGlyph;
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
}
