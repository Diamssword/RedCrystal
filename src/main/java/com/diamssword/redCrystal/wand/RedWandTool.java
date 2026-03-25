package com.diamssword.redCrystal.wand;

import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.storage.GlobalGlyphSettings;
import com.diamssword.redCrystal.storage.PlayerDatas;
import com.diamssword.redCrystal.storage.RedElementState;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.server.core.asset.type.soundset.config.SoundSet;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;

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
		var hotbar = player.getReference().getStore().getComponent(player.getReference(), InventoryComponent.Hotbar.getComponentType());
		var item = hotbar.getInventory().getItemStack((short) slot);
		if(item != null) {
			hotbar.getInventory().replaceItemStackInSlot((short) slot, item, item.withMetadata("RedCrystalToolSettings", RedWandTool.CODEC, settings));
			var dt = player.getReference().getStore().getComponent(player.getReference(), PlayerDatas.getComponentType());
			if(dt != null) {
				dt.onToolChange();
			}
		}

	}

	public static boolean createRune(ItemStack toolStack, RedElementState state, BlockFace face, @Nullable Ref<EntityStore> player) {
		var tool = getForStack(toolStack);
		if(tool.getSelectedGlyph() != null) {
			var element = state.getElement(face);
			if(element == null) {
				boolean re = state.createElement(face, tool.getSelectedGlyph(), tool.mainSettings);
				if(re) {
					var el = state.getElement(face);
					var world = el.getParent().getChunkRef().getStore().getExternalData().getWorld();
					world.execute(() -> {
						RedComponentDisplayUtils.createTempRune(world.getEntityStore(), el.getParent().getPosition(), face, el);
						if(player != null && player.isValid()) {
							var comp = player.getStore().getComponent(player, PlayerDatas.getComponentType());
							if(comp != null)
								comp.invalidateHovered();
						}
					});


				}
				return re;
			}
		}
		return false;
	}

	public static Holder<EntityStore> dropDust(ComponentAccessor<EntityStore> accessor, int count, Vector3i blockPos, BlockFace face) {

		var vec = RedComponentDisplayUtils.getCenteredPosition(blockPos, face, new Vector2d(0, 0));
		return ItemComponent.generateItemDrop(accessor, new ItemStack("RedCrystal_Red_Sliver", count), vec, new Vector3f(), 0, 0, 0);
	}

	public static void playSound(String type, Vector3i at, Ref<EntityStore> source, ComponentAccessor<EntityStore> accessor) {
		var soundSet = SoundSet.getAssetMap().getAsset("RedCrystalWandSet");

		if(soundSet != null) {
			int soundEventIndex = soundSet.getSoundEventIndices().getOrDefault(type, 0);
			if(soundEventIndex != 0) {
				SoundUtil.playSoundEvent3d(source, soundEventIndex, at.x + 0.5, at.y + 0.5, at.z + 0.5, accessor);
			}
		}
	}
}
