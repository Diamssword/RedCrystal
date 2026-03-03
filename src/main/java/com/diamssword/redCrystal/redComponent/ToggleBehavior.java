package com.diamssword.redCrystal.redComponent;

import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.diamssword.redCrystal.storage.RedElement;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Map;

public class ToggleBehavior extends RedCompBehavior {
	public ToggleBehavior(String id, RedElement parent) {
		super(id, parent);
	}

	@Override
	public short maxInputs() {
		return 1;
	}

	@Override
	public short maxOutputs() {
		return 1;
	}

	@Override
	void onSignalChange(short input, short oldValue, short value) {
		if(value > MIN)
			runNextTick(() -> setOutput((short) 0, stateOutput[0] == MIN ? value : MIN));
	}

}
