package com.diamssword.redCrystal.wand;

import au.ellie.hyui.builders.HyUIPage;
import au.ellie.hyui.builders.ImageBuilder;
import au.ellie.hyui.builders.LabelBuilder;
import au.ellie.hyui.builders.PageBuilder;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.html.TemplateProcessor;
import com.diamssword.redCrystal.RedCrystalPlugin;
import com.diamssword.redCrystal.storage.Glyph;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.ArrayList;
import java.util.List;


public class GlyphMenu {

	private static record SimpleGlyph(String id, String name, String url) {}

	private static record Line(List<SimpleGlyph> glyphs) {}

	public static HyUIPage openMenu(PlayerRef ref) {
		var player = ref.getReference().getStore().getComponent(ref.getReference(), Player.getComponentType());
		ItemStack stack = player.getInventory().getActiveHotbarItem();
		if(stack != null) {
			var toolSettings = RedWandTool.getForStack(stack);
			String glyph = null;
			int slot = player.getInventory().getActiveHotbarSlot();
			var map = RedCrystalPlugin.GlyphAssets.getAssetMap().getAssetMap();
			var l = 0;
			var list = new ArrayList<Line>();
			var line = new Line(new ArrayList<>());
			list.add(line);
			for(String id : map.keySet()) {
				if(glyph == null && id.equals(toolSettings.getSelectedGlyph()))
					glyph = id;
				var asset = map.get(id);
				line.glyphs.add(new SimpleGlyph(asset.getId(), translate(ref, asset.getTranslationProperties().getName()), parseUrl(asset.getIcon())));
				l++;
				if(l == 4) {
					line = new Line(new ArrayList<>());
					list.add(line);
					l = 0;
				}
			}
			TemplateProcessor template = new TemplateProcessor()
					.setVariable("lines", list);
			var prototype = PageBuilder.detachedPage().loadHtml("Pages/RedCrystal/GlyphMenu.html", template)
					.withLifetime(CustomPageLifetime.CanDismiss);
			map.keySet().forEach(id -> {
				prototype.addEventListener("RunePanel" + id, CustomUIEventBindingType.Activating, (_, ctx) -> {
					toolSettings.setSelectedGlyph(id);

					RedWandTool.updateToolStack(player, slot, toolSettings);
				});
				prototype.addEventListener("RunePanel" + id, CustomUIEventBindingType.MouseEntered, (_, ctx) -> {
					setupInfoPanel(ref, ctx, map.get(id));
				});
			});


			return prototype.open(ref, ref.getReference().getStore());
		}
		return null;
	}

	private static String parseUrl(String filePath) {
		return filePath.replace("UI/Custom/", "");
	}

	private static void setupInfoPanel(PlayerRef ref, UIContext ctx, Glyph asset) {
		ctx.getById("selected.image", ImageBuilder.class).ifPresent(im -> im.withImage(parseUrl(asset.getIcon())));
		ctx.getById("selected.title", LabelBuilder.class).ifPresent(im -> im.withText(translate(ref, asset.getTranslationProperties().getName())));
		ctx.getById("selected.desc", LabelBuilder.class).ifPresent(im -> im.withText(translate(ref, asset.getTranslationProperties().getDescription())));
		ctx.updatePage(false);
	}

	private static String translate(PlayerRef ref, String translateString) {
		var tr = I18nModule.get().getMessage(ref.getLanguage(), translateString);
		return tr != null ? tr : translateString;
	}

	private static String translate(PlayerRef ref, String glyph, String suffix) {
		var tr = I18nModule.get().getMessage(ref.getLanguage(), "server.RedCrystal.glyph." + glyph + "." + suffix);
		return tr != null ? tr : glyph;
	}
}