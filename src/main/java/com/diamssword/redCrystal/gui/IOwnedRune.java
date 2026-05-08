package com.diamssword.redCrystal.gui;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.UUID;

public interface IOwnedRune {
	public UUID ownerID();

	public void setowner(UUID id);

	public default boolean canUse(UUID user) {
		if(this.ownerID() == null) {
			this.setowner(user);
			return true;
		}
		return this.ownerID().equals(user);
	}

	public default boolean canUse(PlayerRef user) {
		return canUse(user.getUuid());
	}
}
