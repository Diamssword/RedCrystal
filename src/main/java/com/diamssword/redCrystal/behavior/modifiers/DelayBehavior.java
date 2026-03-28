package com.diamssword.redCrystal.behavior.modifiers;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;

public class DelayBehavior extends RedCompBehavior<BehaviorAssetWithSettings.BehaviorAssetSteps> {
	public DelayBehavior(String id, RedElement parent, BehaviorAssetWithSettings.BehaviorAssetSteps asset) {
		super(id, parent, asset);
	}

	@Override
	public void onSignalChange(short input, short oldValue, short value) {
		timers.add(() -> setAllOutput(value), asset.steps);
	}

}
