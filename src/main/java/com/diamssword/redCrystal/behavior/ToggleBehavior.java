package com.diamssword.redCrystal.behavior;

import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.diamssword.redCrystal.storage.RedElement;

public class ToggleBehavior extends RedCompBehavior<BehaviorAsset> {
	public ToggleBehavior(String id, RedElement parent, BehaviorAsset asset) {
		super(id, parent, asset);
	}

	@Override
	void onSignalChange(short input, short oldValue, short value) {
		if(value > MIN && oldValue == MIN)
			runNextTick(() -> setOutput((short) 0, getOutputState(0) == MIN ? value : MIN));
	}

}
