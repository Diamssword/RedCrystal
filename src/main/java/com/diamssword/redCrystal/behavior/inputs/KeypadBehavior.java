package com.diamssword.redCrystal.behavior.inputs;

import com.diamssword.redCrystal.behavior.base.RedCompBehaviorWithModel;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.diamssword.redCrystal.gui.GlyphSettingsValidators;
import com.diamssword.redCrystal.gui.IOwnedRune;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSwitchModels;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.joml.Vector2d;

import java.util.Map;
import java.util.UUID;

public class KeypadBehavior extends RedCompBehaviorWithModel<BehaviorAssetWithSwitchModels, KeypadBehavior.KeypadSettings> {
	public static BuilderCodec<KeypadSettings> CODEC = PickedModelSettings.addToCodecHideTexture(BuilderCodec.builder(KeypadSettings.class, KeypadSettings::new)
			.append(new KeyedCodec<>("KeypadBehaviorTime", Codec.FLOAT), (a, b) -> a.holdTime = b, a -> a.holdTime).addValidator(new GlyphSettingsValidators.StepRangeValidator<>(0.1f, 60f, 0.1f)).add()
			.append(new KeyedCodec<>("KeypadBehaviorCode", Codec.STRING), (a, b) -> a.code = b, a -> a.code).addValidator(new GlyphSettingsValidators.PassCodeValidator(10)).add()
			.append(new KeyedCodec<>("KeypadBehaviorOwner", Codec.UUID_BINARY), (a, b) -> a.owner = b, a -> a.owner).addValidator(new GlyphSettingsValidators.HiddenSettingValidator<>()).add()).build();

	public String typedChars = "";

	public KeypadBehavior(String id, RedElement parent, BehaviorAssetWithSwitchModels asset) {
		super(id, parent, asset);
		this.setSettingsChangeListener(_ -> onSettingsChange());
	}

	private void onSettingsChange() {
		if(parent.getEntities() != null) {
			for(int i = 0; i < 11; i++) {
				var name = "button";
				if(i > 1)
					name = name + (i - 1);
				else
					name = name + (i == 0 ? "C" : "A");
				var bt = parent.getEntities().getOther(name);
				if(bt != null && bt.isValid()) {

					getModel().switchModel(this, bt, false, 0.6f, (i - 1) + "");
				}
			}
		}
	}

	@Override
	public void onSignalChange(short input, short oldValue, short value) {

	}

	@Override
	public void onEntityInteract(String type, short index, Ref<EntityStore> player, Ref<EntityStore> entity, InteractionContext context, InteractType action) {
		super.onEntityInteract(type, index, player, entity, context, action);
		if(type.startsWith("button")) {
			if(action == InteractType.Use)
				onMainRuneInteract(player, entity, context, action);
			else if(action == InteractType.Interact) {
				var trueCode = getSettings().code;
				if(trueCode != null && !trueCode.isBlank()) {
					if(type.endsWith("A"))
						typedChars = "";
					else if(!type.endsWith("C")) {
						typedChars += type.substring(type.length() - 1);
					}
					if(typedChars.length() == getSettings().code.length()) {
						boolean succ;
						if(typedChars.equals(trueCode)) {
							pulseAllOutput(MAX, (int) (getSettings().holdTime * 10));
							succ = true;
						} else {succ = false;}
						typedChars = "";
						execute(() -> {
							var bt = parent.getEntities().getOther("buttonC");
							if(bt != null && bt.isValid()) {
								getModel().switchModel(this, bt, false, 0.6f, succ ? "r" : "w");
								this.timers.add(() -> {
									if(bt.isValid())
										getModel().switchModel(this, bt, false, 0.6f, "");
								}, 10);
							}
						});

					}

				}
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

		float x = -0.25f;
		float y = 0.38f;
		for(int i = 0; i < 11; i++) {
			var name = "button";
			if(i > 1)
				name = name + (i - 1);
			else
				name = name + (i == 0 ? "C" : "A");
			var holder = this.getModel().createEntity(store, this, false, 0.6f, false, (i - 1) + "");
			if(i > 0) {
				holder.ensureComponent(Interactable.getComponentType());
				Interactions interactions = new Interactions();
				interactions.setInteractionId(InteractionType.Use, "*UseRedCrystalEntity");
				holder.addComponent(Interactions.getComponentType(), interactions);
			}
			holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent(name, (short) 0, this.parent));
			var trans = RedComponentDisplayUtils.getCenteredTransform(parent.getParent().getPosition(), facing, new Vector2d(x, y));

			holder.replaceComponent(TransformComponent.getComponentType(), trans);

			res.put(name, holder);
			if(i == 0)
				x += 0.25f;
			x += 0.25f;
			if(i == 1 || i == 4 || i == 7) {
				x = -0.25f;
				y -= 0.25f;
			}
		}

		return res;
	}

	public static class KeypadSettings extends PickedModelSettings implements IOwnedRune {
		public float holdTime = 1;
		public String code;
		public UUID owner;

		@Override
		public UUID ownerID() {
			return owner;
		}

		@Override
		public void setowner(UUID id) {
			this.owner = id;
		}
	}
}
