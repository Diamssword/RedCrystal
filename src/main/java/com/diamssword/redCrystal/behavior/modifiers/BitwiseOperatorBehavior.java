package com.diamssword.redCrystal.behavior.modifiers;

import com.diamssword.redCrystal.behavior.base.RedCompBehaviorWithSettings;
import com.diamssword.redCrystal.storage.GlobalGlyphSettings;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class BitwiseOperatorBehavior extends RedCompBehaviorWithSettings<BehaviorAsset, BitwiseOperatorBehavior.BitwiseSettings> {

	public static BuilderCodec<BitwiseOperatorBehavior.BitwiseSettings> CODEC = BuilderCodec.builder(BitwiseSettings.class, BitwiseSettings::new)
			.append(new KeyedCodec<>("BitwiseOperatorBehaviorOperation", new GlobalGlyphSettings.TypedEnumCodec<>(Mode.class)), (a, b) -> a.operation = b, a -> a.operation)
			.add().build();

	public BitwiseOperatorBehavior(String id, RedElement parent, BehaviorAsset asset) {
		super(id, parent, asset);
	}


	@Override
	public void onSignalChange(short input, short oldValue, short value) {
		var A = getInputState(0);
		var B = getInputState(1);
		var res = 0;
		switch(getSettings().operation) {
			case AND -> res = A & B;
			case OR -> res = A | B;
			case XOR -> res = A ^ B;
			case NOT -> res = ~A;
			case LeftShift -> res = A << B;
			case SignedRightShift -> res = A >> B;
			case UnsignedRightShift -> res = A >>> B;
		}
		setAllOutput((short) Math.clamp(res, MIN, MAX));
	}

	public static enum Mode {
		AND,
		OR,
		XOR,
		NOT,
		LeftShift,
		SignedRightShift,
		UnsignedRightShift;
	}

	public static class BitwiseSettings {
		public Mode operation = Mode.AND;


		public BitwiseSettings() {

		}
	}
}
