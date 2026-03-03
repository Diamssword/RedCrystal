package com.diamssword.redCrystal.display;

import com.diamssword.redCrystal.RedCrystalPlugin;
import com.diamssword.redCrystal.storage.RedElement;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RedEntityHiddenComponent implements Component<EntityStore> {
	public final float baseScale;
	public final RedElement parent;
	private int lightUpState = 0;
	private Set<PlayerRef> seeingPlayers = new HashSet<>();

	public static ComponentType<EntityStore, RedEntityHiddenComponent> getComponentType() {
		return RedCrystalPlugin.RedEntityHiddenComponent;
	}

	public RedEntityHiddenComponent(RedElement parent, float baseScale) {
		this.baseScale = baseScale;
		this.parent = parent;
	}

	public void addSeeingPlayer(Ref<EntityStore> player) {
		var comp = player.getStore().getComponent(player, PlayerRef.getComponentType());
		if(comp != null) {
			seeingPlayers.add(comp);
		}
	}

	public void removeSeeingPlayer(Ref<EntityStore> player) {
		var comp = player.getStore().getComponent(player, PlayerRef.getComponentType());
		if(comp != null) {
			seeingPlayers.remove(comp);
		}
	}

	public Set<PlayerRef> getSeeingPlayers() {
		return new HashSet<>(seeingPlayers);
	}

	public boolean needLightUpSync() {
		return lightUpState == 1 || lightUpState == 3;
	}

	public boolean isLightUp() {
		return lightUpState == 1 || lightUpState == 2;
	}

	public void setLightUp(boolean light) {
		if(light && lightUpState != 2) {
			lightUpState = lightUpState == 3 ? 2 : 1;
		} else if(!light && lightUpState != 0) {
			lightUpState = lightUpState == 1 ? 0 : 3;
		}
	}

	public void setLightUpState(int lightUpState) {
		this.lightUpState = lightUpState;
	}

	@NullableDecl
	@Override
	public Component<EntityStore> clone() {
		return new RedEntityHiddenComponent(this.parent, this.baseScale);
	}
}
