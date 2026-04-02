package com.diamssword.redCrystal.storage.assets;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.display.ModelUtils;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class BehaviorAssetWithModelSwitch extends AbstractBehaviorAsset<BehaviorAssetWithModelSwitch> {
	public static final String EMPTY_TEXTURE = "Items/RedCrystal/Glyphs/Empty.png";

	public static BuilderCodec<BehaviorAssetWithModelSwitch> getCODEC(String id) {
		return BuilderCodec.builder(BehaviorAssetWithModelSwitch.class, () -> new BehaviorAssetWithModelSwitch(id))
				.appendInherited(
						new KeyedCodec<>("ModelOn", Codec.STRING), (model, s) -> model.modelOn = s, model -> model.modelOn, (model, parent) -> model.modelOn = parent.modelOn
				)
				.addValidator(ModelAsset.VALIDATOR_CACHE.getValidator())
				.addValidator(Validators.nonNull())
				.add()
				.appendInherited(
						new KeyedCodec<>("ModelOff", Codec.STRING), (model, s) -> model.modelOff = s, model -> model.modelOff, (model, parent) -> model.modelOff = parent.modelOff
				)
				.addValidator(ModelAsset.VALIDATOR_CACHE.getValidator())
				.addValidator(Validators.nonNull())
				.add().build();

	}

	private String modelOn;
	private String modelOff;

	public BehaviorAssetWithModelSwitch(String id) {
		super(id);
	}

	private ModelAsset getModel(String id) {
		return ModelAsset.getAssetMap().getAsset(id);
	}

	public ModelAsset getOffModel(RedCompBehavior<BehaviorAssetWithModelSwitch> behavior) {
		return getModel(modelOff);
	}

	public ModelAsset getOnModel(RedCompBehavior<BehaviorAssetWithModelSwitch> behavior) {
		return getModel(modelOn);
	}

	public ModelAsset getModel(RedCompBehavior<BehaviorAssetWithModelSwitch> behavior, boolean isOn) {
		return isOn ? getOnModel(behavior) : getOffModel(behavior);
	}

	public void switchModel(RedCompBehavior<BehaviorAssetWithModelSwitch> behavior, Ref<EntityStore> ref, boolean isOn) {
		switchModel(behavior, ref, isOn, 1f, false);
	}

	public void switchModel(RedCompBehavior<BehaviorAssetWithModelSwitch> behavior, Ref<EntityStore> ref, boolean isOn, float scale, boolean isInvisible) {
		var mod = Model.createScaledModel(getModel(behavior, isOn), scale);
		if(isInvisible)
			mod = ModelUtils.withTexture(mod, EMPTY_TEXTURE);
		var model = RedComponentDisplayUtils.modifyBoundingBox(mod, behavior.parent.getFace());
		ref.getStore().replaceComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
	}

	public void switchModel(RedCompBehavior<BehaviorAssetWithModelSwitch> behavior, Ref<EntityStore> ref, boolean isOn, float scale) {
		var model = RedComponentDisplayUtils.modifyBoundingBox(Model.createScaledModel(getModel(behavior, isOn), scale), behavior.parent.getFace());
		ref.getStore().replaceComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
	}

	public Holder<EntityStore> createEntity(EntityStore store, RedCompBehavior<BehaviorAssetWithModelSwitch> behavior, boolean isOn) {
		return createEntity(store, behavior, isOn, 1f, false);
	}

	public Holder<EntityStore> createEntity(EntityStore store, RedCompBehavior<BehaviorAssetWithModelSwitch> behavior, boolean isOn, float scale) {
		return createEntity(store, behavior, isOn, scale, false);
	}

	public Holder<EntityStore> createEntity(EntityStore store, RedCompBehavior<BehaviorAssetWithModelSwitch> behavior, boolean isOn, float scale, boolean isInvisible) {
		var holder = RedComponentDisplayUtils.createMinimalDisplayEntity(store, behavior.parent.getParent().getPosition(), behavior.parent.getFace());
		var mod = Model.createScaledModel(getModel(behavior, isOn), scale);
		if(isInvisible)
			mod = ModelUtils.withTexture(mod, EMPTY_TEXTURE);
		var model = RedComponentDisplayUtils.modifyBoundingBox(mod, behavior.parent.getFace());
		//server crash when updating model without this
		holder.ensureComponent(MovementStatesComponent.getComponentType());
		holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
		return holder;
	}
}
