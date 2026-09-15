package com.diamssword.redCrystal.behavior.modifiers;

import com.diamssword.redCrystal.behavior.base.RedCompBehaviorWithSettings;
import com.diamssword.redCrystal.gui.GlyphSettingsValidators;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class RangeDecoder extends RedCompBehaviorWithSettings<BehaviorAsset, RangeDecoder.BoundsSettings> {

	public static BuilderCodec<BoundsSettings> CODEC = BuilderCodec.builder(BoundsSettings.class, BoundsSettings::new)
			.append(new KeyedCodec<Integer>("RangeDecoderBehaviorBoundMin", BuilderCodec.INTEGER), (a, b) -> a.min = b.shortValue(), a -> (int) a.min)
			.addValidator(new GlyphSettingsValidators.StepRangeValidator<>((int) MIN, (int) MAX, 1)).add()
			.append(new KeyedCodec<Integer>("RangeDecoderBehaviorBoundMax", BuilderCodec.INTEGER), (a, b) -> a.max = b.shortValue(), a -> (int) a.max)
			.addValidator(new GlyphSettingsValidators.StepRangeValidator<>((int) MIN, (int) MAX, 1))
			.add().build();

	public RangeDecoder(String id, RedElement parent, BehaviorAsset asset) {
		super(id, parent, asset);
	}


	@Override
	public void onSignalChange(short input, short oldValue, short value) {

		int mapped = mapClamped(value, getSettings().min, getSettings().max, 0, getSettings().max - 1);
		setAllOutput((short) 0);
		setOutput((short) mapped, MAX);

	}

	public static int mapClamped(
			int value,
			int inMin,
			int inMax,
			int outMin,
			int outMax
	) {
		int mapped = outMin + (value - inMin)
				* (outMax - outMin)
				/ (inMax - inMin);

		return Math.clamp(mapped, outMin, outMax);
	}

	public static class BoundsSettings {
		public short min = MIN;
		public short max = MAX;

		public BoundsSettings() {

		}
	}
}
