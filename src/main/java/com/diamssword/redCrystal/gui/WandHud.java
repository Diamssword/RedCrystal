package com.diamssword.redCrystal.gui;

import com.diamssword.redCrystal.RedCrystalPlugin;
import com.diamssword.redCrystal.storage.PlayerDatas;
import com.diamssword.redCrystal.wand.RedWandTool;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class WandHud extends CustomUIHud {
	public static final String ID = "RedCrystal:WandHud";
	private final PlayerDatas playerDatas;
	private final Player player;

	public WandHud(PlayerRef player, PlayerDatas datas) {
		super(player, ID);
		this.playerDatas = datas;
		this.player = player.getReference().getStore().getComponent(player.getReference(), Player.getComponentType());
	}

	@Override
	protected void build(@Nonnull UICommandBuilder builder) {
		builder.append("Pages/RedCrystal/WandHud.ui");
		onRefreshTool(builder);
		updateHovered(builder);
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

	private void updateHovered(UICommandBuilder builder) {

		var hov = playerDatas.getHovered();
		if(hov == null || !hov.isValid()) {
			builder.set("#SelectedPanel.Visible", false);
		} else {
			builder.set("#SelectedPanel.Visible", true);
			builder.set("#TitleA.Text", getTranslatedName(hov.getAsset().getId()));
			builder.set("#InLine.Text", parseIO(true, hov.getBehavior().getInputValues()));
			builder.set("#OutLine.Text", parseIO(false, hov.getBehavior().getOutputValues()));
		}
	}

	public void showHud() {
		attachHud(this, player);

	}

	public void hide() {
		removeHud(player, getPlayerRef());
	}

	public void refreshTool() {
		if(isStillMyHud(player)) {
			var builder = new UICommandBuilder();
			this.onRefreshTool(builder);
			this.update(false, builder);
		}
	}

	public void refreshHovered() {
		if(isStillMyHud(player)) {
			var builder = new UICommandBuilder();
			this.updateHovered(builder);
			this.update(false, builder);
		}
	}

	private String getTranslatedName(String id) {
		var asset = RedCrystalPlugin.GlyphAssets.getAssetMap().getAssetMap().get(id);
		if(asset != null) {
			return translate(asset.getTranslationProperties().getName());
		}
		return id;
	}

	private String translate(String translateString) {
		var tr = I18nModule.get().getMessage(getPlayerRef().getLanguage(), translateString);
		return tr != null ? tr : translateString;
	}

	private void onRefreshTool(UICommandBuilder builder) {
		var stack = InventoryComponent.getItemInHand(getPlayerRef().getReference().getStore(), getPlayerRef().getReference());
		if(stack != null) {
			var tool = RedWandTool.getForStack(stack);
			var bool = tool.getSelectedGlyph() != null && !tool.getSelectedGlyph().isBlank();
			builder.set("#SelectedPanel.Visible", bool);
			if(bool)
				builder.set("#Selected.Text", "Selected Glyph: " + getTranslatedName(tool.getSelectedGlyph()));

		}
	}

	public static void removeHud(Player player, PlayerRef playerRef) {
		playerRef.getReference().getStore().getExternalData().getWorld().execute(() -> {
			player.getHudManager().removeCustomHud(playerRef, WandHud.ID);
		});
	}

	public static void attachHud(@Nullable CustomUIHud hud, Player player) {
		if(!isStillMyHud(player)) {
			hud.getPlayerRef().getReference().getStore().getExternalData().getWorld().execute(() -> {
				player.getHudManager().addCustomHud(hud.getPlayerRef(), hud);
			});
		}
	}

	public static boolean isStillMyHud(Player player) {
		return player.getHudManager().getCustomHud(WandHud.ID) instanceof WandHud;
	}
}
