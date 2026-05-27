package com.diamssword.redCrystal.behavior.base;

import com.diamssword.redCrystal.gui.GlyphSettingsValidators;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSwitchModels;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nullable;

public abstract class RedCompBehaviorWithModel<T extends BehaviorAssetWithSwitchModels, J extends RedCompBehaviorWithModel.PickedModelSettings> extends RedCompBehaviorWithSettings<T, J> {
	public RedCompBehaviorWithModel(String id, RedElement parent, T asset) {
		super(id, parent, asset);
	}

	public static BuilderCodec<PickedModelSettings> CODEC = PickedModelSettings.addToCodec(BuilderCodec.builder(PickedModelSettings.class, PickedModelSettings::new)).build();

	public BehaviorAssetWithSwitchModels.SwitchModelReference getModel() {

		if(getSettings().pickedModel == null) {
			getSettings().pickedModel = asset.getFirstId();
			this.saveSettings();
		}
		return asset.getModelFor(getSettings().pickedModel);
	}

	@Nullable
	public String getTexture() {
		return getModel().getTexture(getSettings().pickedTexture);
	}

	@Override
	public boolean canShowSetting(String key) {
		if(key.equals("BehaviorWithModelPickedModel"))
			return asset.getModelKeys().size() > 1;
		if(key.equals("BehaviorWithModelPickedTexture"))
			return getModel().getTexturesKey().size() > 1;
		return super.canShowSetting(key);
	}

	public static class PickedModelSettings {
		public String pickedModel = "Default";
		public String pickedTexture = "Default";

		public static <T extends PickedModelSettings, J extends BehaviorAssetWithSwitchModels> BuilderCodec.Builder<T> addToCodec(BuilderCodec.Builder<T> builder) {
			return builder.append(new KeyedCodec<>("BehaviorWithModelPickedModel", BuilderCodec.STRING), (a, b) -> a.pickedModel = b, a -> a.pickedModel).addValidator(new GlyphSettingsValidators.ModelKeySelector(false)).add()
					.append(new KeyedCodec<>("BehaviorWithModelPickedTexture", BuilderCodec.STRING), (a, b) -> a.pickedTexture = b, a -> a.pickedTexture).addValidator(new GlyphSettingsValidators.ModelKeySelector(true)).add();
		}
	}
}
