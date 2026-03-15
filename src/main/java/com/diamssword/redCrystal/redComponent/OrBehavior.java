package com.diamssword.redCrystal.redComponent;

import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
import com.diamssword.redCrystal.storage.RedElement;

public class OrBehavior extends RedCompBehavior<BehaviorAssetWithSettings> {

	public final boolean isAbsolute;

	public OrBehavior(String id, RedElement parent, BehaviorAssetWithSettings asset) {
		super(id, parent, asset);
		isAbsolute = asset.getBoolean("IsAbsolute");
	}


	@Override
	void onSignalChange(short input, short oldValue, short value) {
		if(isAbsolute) {
			int on = 0;
			short maxV = MIN;
			for(int i = 0; i < maxInputs(); i++) {
				var v1 = getState(i);
				if(v1 > MIN) {
					if(v1 > maxV)
						maxV = v1;
					on++;
					if(on == 2) {
						runNextTick(() -> setAllOutput(MIN));
						break;
					}
				}
			}
			if(on < 2) {
				short finalMaxV = maxV;
				runNextTick(() -> setAllOutput(finalMaxV));
			}
		} else {
			if(value > MIN)
				runNextTick(() -> setAllOutput(value));
			else {
				for(int i = 0; i < maxInputs(); i++) {
					if(getState(i) > MIN)
						return;
				}
				runNextTick(() -> setAllOutput(MIN));
			}
		}

	}

}
