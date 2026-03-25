package com.diamssword.redCrystal.behavior;

import com.diamssword.redCrystal.display.ModelUtils;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.diamssword.redCrystal.storage.DisplayState;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Map;

public class LeverBehavior extends RedCompBehavior<BehaviorAsset> {
	public LeverBehavior(String id, RedElement parent, BehaviorAsset asset) {
		super(id, parent, asset);
	}

	@Override
	void onSignalChange(short input, short oldValue, short value) {
	}

	@Override
	public void onEntityInteract(String type, short index, Ref<EntityStore> player, Ref<EntityStore> entity, InteractionContext context, InteractType action) {
		super.onEntityInteract(type, index, player, entity, context, action);
		if(type.equals("lever") && action == InteractType.Interact) {
			short st = getInternalState("lever");
			setAllOutput(st == 0 ? MAX : MIN);
			setInternalState("lever", (short) (st == 0 ? 1 : 0));
			//lightUpRune(this.parent.getEntities().getMain(), st == 0);
			var model = entity.getStore().getComponent(entity, ModelComponent.getComponentType());

			if(model != null) {
				//hold last frame doesn't work consistently
				//AnimationUtils.playAnimation(entity, AnimationSlot.Movement, st == 0 ? "On" : "Off", false, entity.getStore());
				execute(() -> entity.getStore().replaceComponent(entity, ModelComponent.getComponentType(), new ModelComponent(ModelUtils.withModel(model.getModel(), "Items/RedCrystal/Lever" + (st == 0 ? "On" : "") + ".blockymodel", model.getModel().getTexture()))));

			}
		}
	}

	@NullableDecl
	@Override
	public Map<String, Holder<EntityStore>> displayEntities(EntityStore store, BlockFace facing) {
		var res = super.displayEntities(store, facing);
		var holder = RedComponentDisplayUtils.createMinimalDisplayEntity(store, parent.getParent().getPosition(), facing);
		var model = RedComponentDisplayUtils.modifyBoundingBox(Model.createScaledModel(ModelAsset.getAssetMap().getAsset("RedCrystal_Lever"), 0.8f), facing);
		short st = getInternalState("lever");
		model = ModelUtils.withModel(model, "Items/RedCrystal/Lever" + (st == 1 ? "On" : "") + ".blockymodel", model.getTexture());
		//holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(new Box(0, 0, 0, 1, 1, 1)));
		holder.ensureComponent(Interactable.getComponentType());

		//server crash when updating model without this
		holder.ensureComponent(MovementStatesComponent.getComponentType());

		holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
		holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent("lever", (short) 0, this.parent));
		Interactions interactions = new Interactions();
		interactions.setInteractionId(InteractionType.Use, "*UseRedCrystalEntity");  // e.g., "*UseNPC" or custom RootInteraction asset ID
		//interactions.setInteractionHint("your.hint.key");  // Optional client hint text
		holder.addComponent(Interactions.getComponentType(), interactions);
		res.put("lever", holder);
		return res;
	}

}
