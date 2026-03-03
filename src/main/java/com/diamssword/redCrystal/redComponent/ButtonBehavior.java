package com.diamssword.redCrystal.redComponent;

import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.display.RedEntityLinkComponent;
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

public class ButtonBehavior extends RedCompBehavior {
	public ButtonBehavior(String id, RedElement parent) {
		super(id, parent);
	}

	@Override
	public short maxInputs() {
		return 0;
	}

	@Override
	public short maxOutputs() {
		return 1;
	}

	@Override
	void onSignalChange(short input, short oldValue, short value) {

	}

	@Override
	public void onEntityInteract(String type, short index, Ref<EntityStore> player, Ref<EntityStore> entity) {
		super.onEntityInteract(type, index, player, entity);
		if(type.equals("button")) {
			setPulse((short) 0, MAX, 10);
			lightUpRune(this.parent.getEntities().getMain(), true);
			timers.add(() -> lightUpRune(this.parent.getEntities().getMain(), false), 10);
			var model = entity.getStore().getComponent(entity, ModelComponent.getComponentType());
			if(model != null) {
				AnimationUtils.playAnimation(entity, AnimationSlot.Status, "Press", false, entity.getStore());
			}
		}
	}

	@NullableDecl
	@Override
	public Map<String, Holder<EntityStore>> displayEntities(EntityStore store, BlockFace facing) {
		var res = super.displayEntities(store, facing);
		var holder = RedComponentDisplayUtils.createMinimalDisplayEntity(store, parent.getParent().getPosition(), facing);
		var model = RedComponentDisplayUtils.modifyBoundingBox(Model.createScaledModel(ModelAsset.getAssetMap().getAsset("RedCrystal_Button"), 0.5f), facing);
		//holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(new Box(0, 0, 0, 1, 1, 1)));
		holder.ensureComponent(Interactable.getComponentType());
		holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
		holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent("button", (short) 0, this.parent));
		Interactions interactions = new Interactions();
		interactions.setInteractionId(InteractionType.Use, "*UseRedCrystalEntity");  // e.g., "*UseNPC" or custom RootInteraction asset ID
		//interactions.setInteractionHint("your.hint.key");  // Optional client hint text
		holder.addComponent(Interactions.getComponentType(), interactions);
		res.put("button", holder);
		return res;
	}

}
