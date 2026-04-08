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
			player.getHudManager().setCustomHud(playerRef, null);
		});
	}

	public void attachHud(@Nullable CustomUIHud hud, Player player) {
		if(!isStillMyHud(player)) {
			hud.getPlayerRef().getReference().getStore().getExternalData().getWorld().execute(() -> {
				player.getHudManager().setCustomHud(hud.getPlayerRef(), hud);
			});
		}
	}


	public boolean isStillMyHud(Player player) {
		return player.getHudManager().getCustomHud() instanceof WandHud;
	}
}
