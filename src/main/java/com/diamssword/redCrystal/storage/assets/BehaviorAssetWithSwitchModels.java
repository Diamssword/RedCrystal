package com.diamssword.redCrystal.storage.assets;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.display.ModelUtils;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BehaviorAssetWithSwitchModels extends AbstractBehaviorAsset<BehaviorAssetWithSwitchModels> {

	public static BuilderCodec<BehaviorAssetWithSwitchModels> getCODEC(String id) {
		return BuilderCodec.builder(BehaviorAssetWithSwitchModels.class, () -> new BehaviorAssetWithSwitchModels(id))
				.appendInherited(new KeyedCodec<>("Models", new MapCodec<>(SwitchModelReference.CODEC, HashMap::new)), (a, b) -> a.models = b, a -> a.models, (a, b) -> a.models = b.models)
				.addValidator(Validators.nonEmptyMap())
				.add().build();

	}

	public static <T extends BehaviorAssetWithSwitchModels> BuilderCodec.Builder<T> addToCodec(BuilderCodec.Builder<T> codec) {
		return codec
				.appendInherited(new KeyedCodec<>("Models", new MapCodec<>(SwitchModelReference.CODEC, HashMap::new)), (a, b) -> a.models = b, a -> a.models, (a, b) -> a.models = b.models)
				.addValidator(Validators.nonEmptyMap())
				.add();

	}

	public Map<String, SwitchModelReference> models;

	public String getFirstId() {
		return models.keySet().stream().findFirst().get();
	}

	public Set<String> getModelKeys() {
		return models.keySet();
	}

	public SwitchModelReference getModelFor(String id) {
		return models.getOrDefault(id, SwitchModelReference.FALLBACK);
	}

	public BehaviorAssetWithSwitchModels(String id) {
		super(id);
	}

	public static class SwitchModelReference {
		public static final String EMPTY_TEXTURE = "Items/RedCrystal/Glyphs/Empty.png";
		public static SwitchModelReference FALLBACK = new SwitchModelReference("", "");
		public static BuilderCodec<SwitchModelReference> CODEC = BuilderCodec.builder(SwitchModelReference.class, SwitchModelReference::new)
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
				.add()
				.appendInherited(
						new KeyedCodec<>("AltTextures", new MapCodec<>(Codec.STRING, HashMap::new)), (model, s) -> model.altTextures = s, model -> model.altTextures, (model, parent) -> model.altTextures = parent.altTextures
				).add().build();
		private String modelOn;
		private String modelOff;
		private Map<String, String> altTextures;

		public SwitchModelReference(String modelOn, String modelOff) {
			this.modelOn = modelOn;
			this.modelOff = modelOff;
		}

		public SwitchModelReference() {}

		private ModelAsset getModel(String id) {
			return ModelAsset.getAssetMap().getAsset(id);
		}

		public ModelAsset getOffModel() {
			return getModel(modelOff);
		}

		public ModelAsset getOnModel() {
			return getModel(modelOn);
		}

		public Set<String> getTexturesKey() {
			var set = new HashSet<String>();
			set.add("Default");
			if(altTextures != null)
				set.addAll(altTextures.keySet());
			return set;
		}

		@Nullable
		public String getTexture(String key) {
			if(key == null || altTextures == null)
				return null;
			return altTextures.get(key);
		}

		public ModelAsset getModel(boolean isOn) {
			return isOn ? getOnModel() : getOffModel();
		}

		public void switchModel(RedCompBehavior<?> behavior, Ref<EntityStore> ref, boolean isOn) {
			switchModel(behavior, ref, isOn, 1f, false);
		}

		public void switchModel(RedCompBehavior<?> behavior, Ref<EntityStore> ref, boolean isOn, @Nullable String altTextureKey) {
			switchModel(behavior, ref, isOn, 1f, false, altTextureKey);
		}

		public void switchModel(RedCompBehavior<?> behavior, Ref<EntityStore> ref, boolean isOn, float scale, @Nullable String altTextureKey) {
			var mod = Model.createScaledModel(getModel(isOn), scale);
			var alt = getTexture(altTextureKey);
			if(alt != null) {
				mod = ModelUtils.withTexture(mod, alt);
			}
			var model = RedComponentDisplayUtils.modifyBoundingBox(mod, behavior.parent.getFace());
			ref.getStore().replaceComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
		}

		public void switchModel(RedCompBehavior<?> behavior, Ref<EntityStore> ref, boolean isOn, float scale, boolean isInvisible, @Nullable String altTextureKey) {

			var mod = Model.createScaledModel(getModel(isOn), scale);
			if(isInvisible)
				mod = ModelUtils.withTexture(mod, EMPTY_TEXTURE);
			else {
				var alt = getTexture(altTextureKey);
				if(alt != null) {
					mod = ModelUtils.withTexture(mod, alt);
				}
			}
			var model = RedComponentDisplayUtils.modifyBoundingBox(mod, behavior.parent.getFace());
			ref.getStore().replaceComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
		}

		public void switchModel(RedCompBehavior<?> behavior, Ref<EntityStore> ref, boolean isOn, float scale, boolean isInvisible) {
			var mod = Model.createScaledModel(getModel(isOn), scale);
			if(isInvisible)
				mod = ModelUtils.withTexture(mod, EMPTY_TEXTURE);
			var model = RedComponentDisplayUtils.modifyBoundingBox(mod, behavior.parent.getFace());
			ref.getStore().replaceComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
		}

		public void switchModel(RedCompBehavior<?> behavior, Ref<EntityStore> ref, boolean isOn, float scale) {
			var model = RedComponentDisplayUtils.modifyBoundingBox(Model.createScaledModel(getModel(isOn), scale), behavior.parent.getFace());
			ref.getStore().replaceComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
		}

		public Holder<EntityStore> createEntity(EntityStore store, RedCompBehavior<?> behavior, boolean isOn) {
			return createEntity(store, behavior, isOn, 1f, false);
		}

		public Holder<EntityStore> createEntity(EntityStore store, RedCompBehavior<?> behavior, boolean isOn, @Nullable String altTextureKey) {
			return createEntity(store, behavior, isOn, 1f, false, altTextureKey);
		}

		public Holder<EntityStore> createEntity(EntityStore store, RedCompBehavior<?> behavior, boolean isOn, float scale) {
			return createEntity(store, behavior, isOn, scale, false);
		}

		public Holder<EntityStore> createEntity(EntityStore store, RedCompBehavior<?> behavior, boolean isOn, float scale, boolean isInvisible) {return createEntity(store, behavior, isOn, scale, isInvisible, null);}

		public Holder<EntityStore> createEntity(EntityStore store, RedCompBehavior<?> behavior, boolean isOn, float scale, boolean isInvisible, @Nullable String altTextureKey) {
			var holder = RedComponentDisplayUtils.createMinimalDisplayEntity(store, behavior.parent.getParent().getPosition(), behavior.parent.getFace());
			var mod = Model.createScaledModel(getModel(isOn), scale);
			if(isInvisible)
				mod = ModelUtils.withTexture(mod, EMPTY_TEXTURE);
			else {
				var alt = getTexture(altTextureKey);
				if(alt != null) {
					mod = ModelUtils.withTexture(mod, alt);
				}
			}
			var model = RedComponentDisplayUtils.modifyBoundingBox(mod, behavior.parent.getFace());
			//server crash when updating model without this
			holder.ensureComponent(MovementStatesComponent.getComponentType());
			holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
			return holder;
		}
	}


}
