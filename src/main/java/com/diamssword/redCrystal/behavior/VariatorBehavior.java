package com.diamssword.redCrystal.behavior;

import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
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
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Map;

public class VariatorBehavior extends RedCompBehavior<BehaviorAssetWithSettings.BehaviorAssetSteps> {
	private final short stepCount;
	private final short stepRange;
	private final float rotationBase;

	public VariatorBehavior(String id, RedElement parent, BehaviorAssetWithSettings.BehaviorAssetSteps asset) {
		super(id, parent, asset);
		stepCount = asset.steps;
		stepRange = (short) (MAX / (stepCount - 1));
		rotationBase = 300f / stepCount;
	}

	@Override
	void onSignalChange(short input, short oldValue, short value) {

	}

	@Override
	public void onEntityInteract(String type, short index, Ref<EntityStore> player, Ref<EntityStore> entity, InteractionContext context, InteractType action) {
		super.onEntityInteract(type, index, player, entity, context, action);
		if(type.equals("variator") && action == InteractType.Interact) {
			short st = getInternalState("variator");
			st++;
			if(st == stepCount)
				st = 0;
			setAllOutput((short) (stepRange * st));
			setInternalState("variator", st);
			var trans = entity.getStore().getComponent(entity, TransformComponent.getComponentType());
			var rot = RedComponentDisplayUtils.rotationWithTilt(this.parent.getFace(), rotationBase * st);
			trans.setRotation(rot);
			execute(() -> {entity.getStore().replaceComponent(entity, TransformComponent.getComponentType(), trans);});
		}
	}


	@NullableDecl
	@Override
	public Map<String, Holder<EntityStore>> displayEntities(EntityStore store, BlockFace facing) {
		var res = super.displayEntities(store, facing);
		var holder = RedComponentDisplayUtils.createMinimalDisplayEntity(store, parent.getParent().getPosition(), facing);
		var model = RedComponentDisplayUtils.modifyBoundingBox(Model.createScaledModel(ModelAsset.getAssetMap().getAsset("RedCrystal_Variator"), 0.5f), facing);
		short st = getInternalState("variator");
		var rot = RedComponentDisplayUtils.rotationWithTilt(this.parent.getFace(), rotationBase * st);
		var trans = holder.getComponent(TransformComponent.getComponentType());
		trans.setRotation(rot);
		//holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(new Box(0, 0, 0, 1, 1, 1)));
		holder.ensureComponent(Interactable.getComponentType());

		//server crash when updating model without this
		holder.ensureComponent(MovementStatesComponent.getComponentType());

		holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
		holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent("variator", (short) 0, this.parent));
		Interactions interactions = new Interactions();
		interactions.setInteractionId(InteractionType.Use, "*UseRedCrystalEntity");  // e.g., "*UseNPC" or custom RootInteraction asset ID
		//interactions.setInteractionHint("your.hint.key");  // Optional client hint text
		holder.addComponent(Interactions.getComponentType(), interactions);
		res.put("variator", holder);
		return res;
	}

}
