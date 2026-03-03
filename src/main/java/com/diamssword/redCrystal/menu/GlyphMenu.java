package com.diamssword.redCrystal.menu;

import au.ellie.hyui.builders.HyUIPage;
import au.ellie.hyui.builders.PageBuilder;
import au.ellie.hyui.builders.PanelBuilder;
import au.ellie.hyui.html.TemplateProcessor;
import com.diamssword.redCrystal.redComponent.RedComponentRegister;
import com.diamssword.redCrystal.storage.RedWandStorage;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.ArrayList;
import java.util.List;


public class GlyphMenu {
	private static PageBuilder prototype;

	private static record Glyph(String name) {}

	private static record Line(List<Glyph> glyphs) {}

	private static void create() {


	}

	public static HyUIPage openMenu(PlayerRef ref) {
		var player = ref.getReference().getStore().getComponent(ref.getReference(), Player.getComponentType());
		ItemStack stack = player.getInventory().getActiveHotbarItem();
		var toolSettings = stack.getFromMetadataOrDefault("RedCrystalToolSettings", RedWandStorage.CODEC);
		int slot = player.getInventory().getActiveHotbarSlot();
		var ids = RedComponentRegister.getAllIds();
		var l = 0;
		var list = new ArrayList<Line>();
		var line = new Line(new ArrayList<>());
		list.add(line);
		for(String id : ids) {
			line.glyphs.add(new Glyph(id));
			l++;
			if(l == 4) {
				line = new Line(new ArrayList<>());
				list.add(line);
				l = 0;
			}
		}
		TemplateProcessor template = new TemplateProcessor()
				.setVariable("lines", list);
		prototype = PageBuilder.detachedPage().loadHtml("Pages/RedCrystal/GlyphMenu.html", template)
				.withLifetime(CustomPageLifetime.CanDismiss);
		ids.forEach(id -> {
			prototype.addEventListener("RunePanel" + id, CustomUIEventBindingType.Activating, (_, ctx) -> {
				toolSettings.setSelectedGlyph(id);
				updateStack(player, slot, stack.getItemId(), toolSettings);
			});
			prototype.addEventListener("RunePanel" + id, CustomUIEventBindingType.MouseEntered, (_, ctx) -> {
				System.out.println("hover" + id);
			});
		});
		return prototype.open(ref, ref.getReference().getStore());
	}

	private static void updateStack(Player player, int slot, String itemID, RedWandStorage settings) {
		var item = player.getInventory().getHotbar().getItemStack((short) slot);
		if(item != null && item.getItemId().equals(itemID)) {
			player.getInventory().getHotbar().replaceItemStackInSlot((short) slot, item, item.withMetadata("RedCrystalToolSettings", RedWandStorage.CODEC, settings));
		}

	}
}