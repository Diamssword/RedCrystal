package com.diamssword.redCrystal.gui;

import au.ellie.hyui.builders.*;
import com.diamssword.redCrystal.RedCrystalPlugin;
import com.diamssword.redCrystal.storage.PlayerDatas;
import com.diamssword.redCrystal.wand.RedWandTool;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.List;
import java.util.Optional;

public class WandHud {
	private final PlayerRef playerRef;
	private final PlayerDatas playerDatas;
	private HyUIHud toolHud;
	private HyUIHud hoveredHud;

	public WandHud(PlayerRef player, PlayerDatas datas) {
		this.playerRef = player;
		this.playerDatas = datas;
		create();
	}

	public void create() {

		var hud = HudBuilder.hudForPlayer(playerRef).loadHtml("Pages/RedCrystal/WandHudTool.html").onRefresh(this::onRefreshTool);
		var tool = RedWandTool.getForStack(InventoryComponent.getItemInHand(playerRef.getReference().getStore(), playerRef.getReference()));
		hud.getById("SelectedPanel", GroupBuilder.class).ifPresent(p -> {
			p.withVisible(tool.getSelectedGlyph() != null && !tool.getSelectedGlyph().isBlank());
			hud.getById("Selected", LabelBuilder.class).ifPresent(l -> {
				l.withText("Selected Glyph: " + getTranslatedName(tool.getSelectedGlyph()));
			});
		});
		var hud1 = HudBuilder.hudForPlayer(playerRef).loadHtml("Pages/RedCrystal/WandHudHover.html").withRefreshRate(500).onRefresh(ui -> {updateHovered(ui::getById);});
		updateHovered(hud1::getById);
		playerRef.getReference().getStore().getExternalData().getWorld().execute(() -> {
			toolHud = hud.show(playerRef);
			hoveredHud = hud1.show(playerRef);
		});

	}

	@FunctionalInterface
	interface ElementGetter {
		<E extends UIElementBuilder<E>> Optional<E> apply(String id, Class<E> clazz);
	}

	private String parseIO(boolean in, List<Short> values) {
		var word = in ? "In" : "Out";
		if(values.isEmpty())
			return "";
		if(values.size() == 1) {
			return "Signal " + word + ": " + values.getFirst();
		}
		StringBuilder val = new StringBuilder("Signals " + word + ": ");
		for(int i = 0; i < values.size(); i++) {

			if(i != 0)
				val.append("|");
			var pow = values.get(i);
			if(pow < 10)
				val.append(" ").append(pow).append(" ");
			else if(pow < 100)
				val.append(pow).append(" ");
			else
				val.append(pow);
		}
		return val.toString();
	}

	private void updateHovered(ElementGetter getter) {

		var hov = playerDatas.getHovered();
		getter.apply("SelectedPanel", ContainerBuilder.class).ifPresent(c -> {
			if(hov == null || !hov.isValid()) {
				c.withVisible(false);
			} else {
				c.withVisible(true);
				c.withTitleText(getTranslatedName(hov.getAsset().getId()));
				getter.apply("InLine", LabelBuilder.class).ifPresent(l -> {
					l.withText(parseIO(true, hov.getBehavior().getInputValues()));
				});
				getter.apply("OutLine", LabelBuilder.class).ifPresent(l -> {
					l.withText(parseIO(false, hov.getBehavior().getOutputValues()));
				});
			}

		});
	}

	public void show() {
		if(toolHud != null) {
			toolHud.triggerRefresh();
			toolHud.unhide();
		}
		if(hoveredHud != null) {
			hoveredHud.triggerRefresh();
			hoveredHud.unhide();
		}
	}

	public void hide() {
		if(toolHud != null)
			toolHud.hide();
		if(hoveredHud != null)
			hoveredHud.hide();
	}

	public void refreshTool() {
		toolHud.triggerRefresh();
	}

	public void refreshHovered() {
		hoveredHud.triggerRefresh();
		hoveredHud.refreshOrRerender(true, false);
	}

	private String getTranslatedName(String id) {
		var asset = RedCrystalPlugin.GlyphAssets.getAssetMap().getAssetMap().get(id);
		if(asset != null) {
			return translate(asset.getTranslationProperties().getName());
		}
		return id;
	}

	private String translate(String translateString) {
		var tr = I18nModule.get().getMessage(playerRef.getLanguage(), translateString);
		return tr != null ? tr : translateString;
	}

	private void onRefreshTool(HyUIHud hud) {
		var stack = InventoryComponent.getItemInHand(playerRef.getReference().getStore(), playerRef.getReference());
		if(stack != null) {
			var tool = RedWandTool.getForStack(stack);
			hud.getById("SelectedPanel", GroupBuilder.class).ifPresent(p -> {
				p.withVisible(tool.getSelectedGlyph() != null && !tool.getSelectedGlyph().isBlank());
				hud.getById("Selected", LabelBuilder.class).ifPresent(l -> {
					l.withText("Selected Glyph: " + getTranslatedName(tool.getSelectedGlyph()));
				});
			});
		}
	}
}
