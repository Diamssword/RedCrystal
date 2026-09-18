package com.diamssword.redCrystal.behavior.modifiers;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.behavior.base.RedCompBehaviorWithSettings;
import com.diamssword.redCrystal.storage.GlobalGlyphSettings;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class RangeEncoder extends RedCompBehaviorWithSettings<BehaviorAsset, RangeEncoder.CumulativeSettings> {

	public static BuilderCodec<CumulativeSettings> CODEC = BuilderCodec.builder(CumulativeSettings.class, CumulativeSettings::new)
			.append(new KeyedCodec<>("RangeEncoderBehaviorCumulative", BuilderCodec.BOOLEAN), (a, b) -> a.cumulative = b, a -> a.cumulative)
			.add().build();

	public RangeEncoder(String id, RedElement parent, BehaviorAsset asset) {
		super(id, parent, asset);
	}


	@Override
	public void onSignalChange(short input, short oldValue, short value) {
		if(getSettings().cumulative) {
			int res = 0;
			int res1 = 0;
			for(int i = 0; i < inputsCount(); i++) {
				var st = getInputState(i);
				if(st > MIN) {
					res += (int) Math.pow(2, i);
					res1 += st;
				}
			}
			setOutput((short) 0, (short) Math.clamp(res, MIN, MAX));
			setOutput((short) 1, (short) Math.clamp(res1, MIN, MAX));
		} else {
			int winning = input;
			for(int i = 0; i < inputsCount(); i++) {
				if(getInputState(i) > MIN)
					winning = i;
			}
			setAllOutput(MIN);
			if(winning == input && value == MIN)
				setOutput((short) 0, (short) 0);
			else
				setOutput((short) 0, (short) (winning + 1));
			setOutput((short) 1, getInputState(winning));
		}
	}

	public static class CumulativeSettings {
		public boolean cumulative = false;


		public CumulativeSettings() {

		}
	}
}
