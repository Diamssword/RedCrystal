package com.diamssword.redCrystal.behavior.inputs;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.behavior.base.RedCompBehaviorWithModel;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.gui.GlyphSettingsValidators;
import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSwitchModels;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Map;

public class ButtonBehavior extends RedCompBehaviorWithModel<BehaviorAssetWithSwitchModels, ButtonBehavior.ButtonSettings> {
	public static BuilderCodec<ButtonSettings> CODEC = PickedModelSettings.addToCodec(BuilderCodec.builder(ButtonSettings.class, ButtonSettings::new)
			.append(new KeyedCodec<>("ButtonBehaviorTime", Codec.FLOAT), (a, b) -> a.holdTime = b, a -> a.holdTime).addValidator(new GlyphSettingsValidators.StepRangeValidator<>(0.1f, 60f, 0.1f)).add()).build();

	public ButtonBehavior(String id, RedElement parent, BehaviorAssetWithSwitchModels asset) {
		super(id, parent, asset);
		this.setSettingsChangeListener(_ -> onSettingsChange());
	}


	private void onSettingsChange() {
		if(parent.getEntities() != null) {
			var plate = parent.getEntities().getOther("button");
			if(plate != null) {
				getModel().switchModel(this, plate, false, getSettings().pickedTexture);
			}

		}
	}

	@Override
	public void onSignalChange(short input, short oldValue, short value) {

	}

	@Override
	public void onEntityInteract(String type, short index, Ref<EntityStore> player, Ref<EntityStore> entity, InteractionContext context, InteractType action) {
		super.onEntityInteract(type, index, player, entity, context, action);
		if(type.equals("button")) {
			if(action == InteractType.Use)
				onMainRuneInteract(player, entity, context, action);
			else if(action == InteractType.Interact) {
				pulseAllOutput(MAX, (int) (getSettings().holdTime * 10));
				var model = entity.getStore().getComponent(entity, ModelComponent.getComponentType());
				if(model != null) {
					AnimationUtils.playAnimation(entity, AnimationSlot.Status, "Press", false, entity.getStore());
				}
			}
		}
	}


	@NullableDecl
	@Override
	public Map<String, Holder<EntityStore>> displayEntities(EntityStore store, BlockFace facing) {
		var res = super.displayEntities(store, facing);
		var holder = this.getModel().createEntity(store, this, false, getSettings().pickedTexture);
		holder.ensureComponent(Interactable.getComponentType());
		holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent("button", (short) 0, this.parent));
		Interactions interactions = new Interactions();
		interactions.setInteractionId(InteractionType.Use, "*UseRedCrystalEntity");
		holder.addComponent(Interactions.getComponentType(), interactions);
		res.put("button", holder);
		return res;
	}

	public static class ButtonSettings extends PickedModelSettings {
		public float holdTime = 1;
	}
}
