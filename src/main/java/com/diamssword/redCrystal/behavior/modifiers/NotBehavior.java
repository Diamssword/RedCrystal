package com.diamssword.redCrystal.behavior.modifiers;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
import com.diamssword.redCrystal.storage.RedElement;

public class NotBehavior extends RedCompBehavior<BehaviorAssetWithSettings.BehaviorAssetBinary> {

	public final boolean isBinary;

	public NotBehavior(String id, RedElement parent, BehaviorAssetWithSettings.BehaviorAssetBinary asset) {
		super(id, parent, asset);
		this.isBinary = asset.isBinary;
		this.setDefaultOutput(MAX);
	}


	@Override
	public void onSignalChange(short input, short oldValue, short value) {

		if(isBinary) {
			runNextTick(() -> setAllOutput(value == MIN ? MAX : MIN));
		} else {
			runNextTick(() -> setAllOutput((short) (MAX - value)));
		}

	}

}
