package com.diamssword.redCrystal.storage;

import com.diamssword.redCrystal.RedCrystalPlugin;
import com.diamssword.redCrystal.wand.LinkingState;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class PlayerDatas implements Component<EntityStore> {
	public static ComponentType<EntityStore, PlayerDatas> getComponentType() {
		return RedCrystalPlugin.RedToolSettingsComponent;
	}

	public final LinkingState linkingState;

	private boolean isToolEquiped;
	public final Set<Ref<EntityStore>> viewedEntities = new HashSet<>();

	public PlayerDatas() {
		linkingState = new LinkingState();
	}

	public PlayerDatas(LinkingState state) {
		this.linkingState = state;
	}

	public boolean isToolEquiped() {
		return isToolEquiped;
	}

	public void setToolEquiped(boolean toolEquiped) {
		isToolEquiped = toolEquiped;
	}

	@Nullable
	@Override
	public Component<EntityStore> clone() {
		var n = new PlayerDatas(linkingState);
		return n;
	}
}
