package com.diamssword.redCrystal.behavior.modifiers;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.behavior.base.RedCompBehaviorWithSettings;
import com.diamssword.redCrystal.behavior.inputs.LaserDetectorBehavior;
import com.diamssword.redCrystal.gui.GlyphSettingsValidators;
import com.diamssword.redCrystal.storage.GlobalGlyphSettings;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class ComparatorBehavior extends RedCompBehaviorWithSettings<BehaviorAsset, ComparatorBehavior.ComparatorSettings> {

	public static BuilderCodec<ComparatorBehavior.ComparatorSettings> CODEC = BuilderCodec.builder(ComparatorSettings.class, ComparatorSettings::new)
			.append(new KeyedCodec<>("ComparatorBehaviorOperation", new GlobalGlyphSettings.TypedEnumCodec<>(Mode.class)), (a, b) -> a.operation = b, a -> a.operation)
			.add().build();

	public ComparatorBehavior(String id, RedElement parent, BehaviorAsset asset) {
		super(id, parent, asset);
	}


	@Override
	public void onSignalChange(short input, short oldValue, short value) {
		var A = getInputState(0);
		var B = getInputState(1);
		boolean res = false;
		switch(getSettings().operation) {
			case Equal -> res = A == B;
			case Greater -> res = A > B;
			case Lower -> res = A < B;
			case GreaterOrEqual -> res = A >= B;
			case LowerOrEqual -> res = A <= B;
		}
		setAllOutput(res ? MAX : MIN);
	}

	public static enum Mode {
		Equal,
		Greater,
		Lower,
		GreaterOrEqual,
		LowerOrEqual;
	}

	public static class ComparatorSettings {
		public Mode operation = Mode.Equal;


		public ComparatorSettings() {

		}
	}
}
