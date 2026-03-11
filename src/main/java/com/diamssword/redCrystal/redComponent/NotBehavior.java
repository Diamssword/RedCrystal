package com.diamssword.redCrystal.redComponent;

import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
import com.diamssword.redCrystal.storage.RedElement;

public class NotBehavior extends RedCompBehavior<BehaviorAssetWithSettings> {

	public final boolean isBinary;

	public NotBehavior(String id, RedElement parent, BehaviorAssetWithSettings asset) {
		super(id, parent, asset);
		this.isBinary = asset.getBoolean("IsBinary");
		this.setAllOutput(MAX);
	}

	@Override
	public short defaultOutputValue(short index) {
		return MAX;
	}

	@Override
	void onSignalChange(short input, short oldValue, short value) {

		if(isBinary) {
			runNextTick(() -> setAllOutput(value == MIN ? MAX : MIN));
		} else {
			runNextTick(() -> setAllOutput((short) (MAX - value)));
		}

	}

}
