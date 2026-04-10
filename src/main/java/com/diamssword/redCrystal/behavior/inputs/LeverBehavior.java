package com.diamssword.redCrystal.behavior.inputs;

import com.diamssword.redCrystal.behavior.base.RedCompBehaviorWithModel;
import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSwitchModels;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Map;

public class LeverBehavior extends RedCompBehaviorWithModel<BehaviorAssetWithSwitchModels, RedCompBehaviorWithModel.PickedModelSettings> {
	public LeverBehavior(String id, RedElement parent, BehaviorAssetWithSwitchModels asset) {
		super(id, parent, asset);
		this.setSettingsChangeListener(_ -> onSettingsChange());
	}

	@Override
	public void onSignalChange(short input, short oldValue, short value) {
	}

	private void onSettingsChange() {
		if(parent.getEntities() != null) {
			var plate = parent.getEntities().getOther("lever");
			if(plate != null) {
				getModel().switchModel(this, plate, getInternalState("lever") == 1, getSettings().pickedTexture);
			}

		}
	}

	@Override
	public void onEntityInteract(String type, short index, Ref<EntityStore> player, Ref<EntityStore> entity, InteractionContext context, InteractType action) {
		super.onEntityInteract(type, index, player, entity, context, action);
		if(type.equals("lever")) {
			if(action == InteractType.Interact) {
				short st = getInternalState("lever");
				setAllOutput(st == 0 ? MAX : MIN);
				setInternalState("lever", (short) (st == 0 ? 1 : 0));
				//lightUpRune(this.parent.getEntities().getMain(), st == 0);
				var model = entity.getStore().getComponent(entity, ModelComponent.getComponentType());

				if(model != null) {
					//hold last frame doesn't work consistently
					//AnimationUtils.playAnimation(entity, AnimationSlot.Movement, st == 0 ? "On" : "Off", false, entity.getStore());
					execute(() -> getModel().switchModel(this, entity, st == 0, getSettings().pickedTexture));
				}
			} else if(action == InteractType.Use) {
				onMainRuneInteract(player, entity, context, action);
			}
		}
	}

	@NullableDecl
	@Override
	public Map<String, Holder<EntityStore>> displayEntities(EntityStore store, BlockFace facing) {
		var res = super.displayEntities(store, facing);

		var holder = this.getModel().createEntity(store, this, getInternalState("lever") == 1, getSettings().pickedTexture);
		holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent("lever", (short) 0, this.parent));
		holder.ensureComponent(Interactable.getComponentType());
		Interactions interactions = new Interactions();
		interactions.setInteractionId(InteractionType.Use, "*UseRedCrystalEntity");
		holder.addComponent(Interactions.getComponentType(), interactions);
		res.put("lever", holder);
		return res;
	}

}
