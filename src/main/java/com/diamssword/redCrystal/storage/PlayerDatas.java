package com.diamssword.redCrystal.storage;

import com.diamssword.redCrystal.RedCrystalPlugin;
import com.diamssword.redCrystal.interaction.WandBlockInteraction;
import com.diamssword.redCrystal.wand.LinkingState;
import com.diamssword.redCrystal.gui.WandHud;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class PlayerDatas implements Component<EntityStore> {
	public static ComponentType<EntityStore, PlayerDatas> getComponentType() {
		return RedCrystalPlugin.RedToolSettingsComponent;
	}

	public final LinkingState linkingState;
	private Vector3i lastHoveredBlock;
	private BlockFace lastHoveredFace;
	private RedElement lastHoveredElement;
	private boolean isToolEquiped;
	private WandHud currentHud;
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
		return new PlayerDatas(linkingState);
	}

	@Nullable
	public RedElement getHovered() {
		return lastHoveredElement;
	}

	public void showHud(PlayerRef player) {
		if(currentHud == null)
			currentHud = new WandHud(player, this);
		else {
			currentHud.show();

		}
	}

	public void hideHud() {
		if(currentHud != null)
			currentHud.hide();
	}

	public void invalidateHovered() {
		lastHoveredElement = null;
		lastHoveredBlock = null;
		lastHoveredFace = null;
		if(currentHud != null)
			currentHud.refreshHovered();
	}

	public void updateHoveredElement(World world, Vector3i pos, BlockFace face) {
		if(pos == null) {
			invalidateHovered();
		} else if(!pos.equals(lastHoveredBlock) || face != lastHoveredFace) {
			lastHoveredBlock = pos;
			lastHoveredFace = face;
			var state = WandBlockInteraction.getBlockState(world, pos.x, pos.y, pos.z);
			if(state != null) {
				lastHoveredElement = state.getElement(face);
			} else
				lastHoveredElement = null;
			if(currentHud != null)
				currentHud.refreshHovered();
		}
	}

	public void onToolChange() {
		if(currentHud != null) {
			currentHud.refreshTool();
		}
	}
}
