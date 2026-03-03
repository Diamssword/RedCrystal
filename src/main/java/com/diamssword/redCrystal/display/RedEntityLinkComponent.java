package com.diamssword.redCrystal.display;

import com.diamssword.redCrystal.RedCrystalPlugin;
import com.diamssword.redCrystal.storage.RedElement;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class RedEntityLinkComponent implements Component<EntityStore> {

	private String part;
	short partIndex;
	RedElement linked;

	public static ComponentType<EntityStore, RedEntityLinkComponent> getComponentType() {
		return RedCrystalPlugin.RedLinkComponent;
	}

	public RedEntityLinkComponent(String part, short partIndex, RedElement linked) {
		this.part = part;
		this.partIndex = partIndex;
		this.linked = linked;
	}

	public String getPart() {
		return part;
	}

	public void setPart(String part) {
		this.part = part;
	}

	public short getPartIndex() {
		return partIndex;
	}

	public void setPartIndex(short partIndex) {
		this.partIndex = partIndex;
	}

	public RedElement getLinked() {
		return linked;
	}

	public void setLinked(RedElement linked) {
		this.linked = linked;
	}

	@NullableDecl
	@Override
	public Component<EntityStore> clone() {
		return new RedEntityLinkComponent(part, partIndex, linked);
	}
}
