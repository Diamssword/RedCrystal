package com.diamssword.redCrystal.gui;

import com.buuz135.mhud.MultipleCustomUIHud;
import com.buuz135.mhud.MultipleHUD;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nullable;

public class MultiHudManager extends HudManager {
	public static final String ID = "RedCrystal:WandHud";

	public MultiHudManager() {

	}

	@Override
	public void removeHud(Player player, PlayerRef playerRef) {
		playerRef.getReference().getStore().getExternalData().getWorld().execute(() -> {
			MultipleHUD.getInstance().hideCustomHud(player, ID);
		});
	}

	@Override
	public void attachHud(@Nullable CustomUIHud hud, Player player) {
		if(!isStillMyHud(player)) {
			MultipleHUD.getInstance().setCustomHud(player, hud.getPlayerRef(), ID, hud);
		} else if(hud == null) {
			MultipleHUD.getInstance().hideCustomHud(player, ID);
		}
	}

	@Override
	public boolean isStillMyHud(Player player) {
		if(player.getHudManager().getCustomHud() instanceof MultipleCustomUIHud hud) {
			return hud.get(ID) != null;
		}
		return false;
	}
}
