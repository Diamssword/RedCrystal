package com.diamssword.redCrystal.behavior.modifiers;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.behavior.base.RedCompBehaviorWithSettings;
import com.diamssword.redCrystal.gui.GlyphSettingsValidators;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;

public class DelayBehavior extends RedCompBehaviorWithSettings<BehaviorAsset, DelayBehavior.DelaySettings> {
	public DelayBehavior(String id, RedElement parent, BehaviorAsset asset) {
		super(id, parent, asset);
	}

	public static BuilderCodec<DelaySettings> CODEC = BuilderCodec.builder(DelaySettings.class, DelaySettings::new)
			.append(new KeyedCodec<>("DelayBehaviorTime", BuilderCodec.FLOAT), (a, b) -> a.delayInSec = b, a -> a.delayInSec)
			.addValidator(new GlyphSettingsValidators.StepRangeValidator<>(0.1f, 60f, 0.1f))
			.add().build();

	@Override
	public void onSignalChange(short input, short oldValue, short value) {
		timers.add(() -> setAllOutput(value), (int) (getSettings().delayInSec * 10));
	}

	public static class DelaySettings {
		float delayInSec = 5;

		public DelaySettings() {}
	}
}
