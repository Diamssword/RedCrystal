package com.diamssword.redCrystal.behavior.inputs;

import com.diamssword.redCrystal.behavior.base.RedCompBehaviorWithModel;
import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
import com.diamssword.redCrystal.worldInteraction.FacingUtil;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.math.vector.Rotation3f;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Map;

public class VariatorBehavior extends RedCompBehaviorWithModel<BehaviorAssetWithSettings.BehaviorAssetSteps, RedCompBehaviorWithModel.PickedModelSettings> {
	private final short stepCount;
	private final short stepRange;
	private final float rotationBase;

	public VariatorBehavior(String id, RedElement parent, BehaviorAssetWithSettings.BehaviorAssetSteps asset) {
		super(id, parent, asset);
		stepCount = asset.steps;
		stepRange = (short) (MAX / (stepCount - 1));
		rotationBase = 300f / stepCount;
		this.setSettingsChangeListener(_ -> onSettingsChange());
	}

	@Override
	public void onSignalChange(short input, short oldValue, short value) {

	}

	private void onSettingsChange() {
		if(parent.getEntities() != null) {
			var plate = parent.getEntities().getOther("selector");
			if(plate != null) {
				getModel().switchModel(this, plate, false, getSettings().pickedTexture);
			}
		}
	}

	@Override
	public void onEntityInteract(String type, short index, Ref<EntityStore> player, Ref<EntityStore> entity, InteractionContext context, InteractType action) {
		super.onEntityInteract(type, index, player, entity, context, action);
		if(type.equals("variator")) {
			if(action == InteractType.Interact) {
				short st = getInternalState("variator");
				st++;
				if(st == stepCount)
					st = 0;
				setAllOutput((short) (stepRange * st));
				setInternalState("variator", st);
				var trans = entity.getStore().getComponent(entity, TransformComponent.getComponentType());
				var rot = FacingUtil.facingToRotationWithTilt(this.parent.getFace(), rotationBase * st);
				trans.setRotation(new Rotation3f(rot.x, rot.y, rot.z));
				execute(() -> {entity.getStore().replaceComponent(entity, TransformComponent.getComponentType(), trans);});
			} else if(action == InteractType.Use) {
				onMainRuneInteract(player, entity, context, action);
			}
		}
	}


	@NullableDecl
	@Override
	public Map<String, Holder<EntityStore>> displayEntities(EntityStore store, BlockFace facing) {
		var res = super.displayEntities(store, facing);
		var holder = this.getModel().createEntity(store, this, false, getSettings().pickedTexture);
		short st = getInternalState("variator");
		var rot = FacingUtil.facingToRotationWithTilt(this.parent.getFace(), rotationBase * st);
		var trans = holder.getComponent(TransformComponent.getComponentType());
		trans.setRotation(new Rotation3f(rot.x, rot.y, rot.z));
		holder.ensureComponent(Interactable.getComponentType());
		holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent("variator", (short) 0, this.parent));
		Interactions interactions = new Interactions();
		interactions.setInteractionId(InteractionType.Use, "*UseRedCrystalEntity");
		holder.addComponent(Interactions.getComponentType(), interactions);
		res.put("variator", holder);
		return res;
	}

}
