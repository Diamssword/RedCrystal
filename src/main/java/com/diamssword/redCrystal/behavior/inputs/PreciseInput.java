package com.diamssword.redCrystal.behavior.inputs;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.diamssword.redCrystal.gui.ValueSelectMenu;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Map;

public class PreciseInput extends RedCompBehavior<BehaviorAsset> {

	public PreciseInput(String id, RedElement parent, BehaviorAsset asset) {
		super(id, parent, asset);
	}

	@Override
	public void onSignalChange(short input, short oldValue, short value) {

	}

	@Override
	public void onEntityInteract(String type, short index, Ref<EntityStore> player, Ref<EntityStore> entity, InteractionContext context, InteractType action) {
		super.onEntityInteract(type, index, player, entity, context, action);
		if(type.equals("selector") && action == InteractType.Interact) {
			var ref = player.getStore().getComponent(player, PlayerRef.getComponentType());
			if(ref != null)
				ValueSelectMenu.openRange(ref, MIN, MAX, getInternalState("selector"), (i) -> {
					if(i != null)
						setInternalState("selector", i.shortValue());
					setAllOutput(getInternalState("selector"));
				});
		}
	}


	@NullableDecl
	@Override
	public Map<String, Holder<EntityStore>> displayEntities(EntityStore store, BlockFace facing) {
		var res = super.displayEntities(store, facing);
		var holder = RedComponentDisplayUtils.createMinimalDisplayEntity(store, parent.getParent().getPosition(), facing);
		var model = RedComponentDisplayUtils.modifyBoundingBox(Model.createScaledModel(ModelAsset.getAssetMap().getAsset("RedCrystal_PreciseInput"), 1f), facing);
		short st = getInternalState("selector");

		holder.ensureComponent(Interactable.getComponentType());

		//server crash when updating model without this
		holder.ensureComponent(MovementStatesComponent.getComponentType());

		holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
		holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent("selector", (short) 0, this.parent));
		Interactions interactions = new Interactions();
		interactions.setInteractionId(InteractionType.Use, "*UseRedCrystalEntity");  // e.g., "*UseNPC" or custom RootInteraction asset ID
		//interactions.setInteractionHint("your.hint.key");  // Optional client hint text
		holder.addComponent(Interactions.getComponentType(), interactions);
		res.put("selector", holder);
		return res;
	}

}
