package com.diamssword.redCrystal.gui;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nullable;

public class HudManager {
	public HudManager() {

	}

	public void removeHud(Player player, PlayerRef playerRef) {
		playerRef.getReference().getStore().getExternalData().getWorld().execute(() -> {
			player.getHudManager().removeCustomHud(playerRef, WandHud.HUD_KEY);
		});
	}

	public void attachHud(@Nullable CustomUIHud hud, Player player) {
		if(!isStillMyHud(player)) {
			hud.getPlayerRef().getReference().getStore().getExternalData().getWorld().execute(() -> {
				player.getHudManager().addCustomHud(hud.getPlayerRef(), hud);
			});
		}
	}


	public boolean isStillMyHud(Player player) {
		return player.getHudManager().getCustomHud(WandHud.HUD_KEY) instanceof WandHud;
	}
}
