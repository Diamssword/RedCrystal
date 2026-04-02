package com.diamssword.redCrystal.behavior.modifiers;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;

public class CalculusBehavior extends RedCompBehavior<BehaviorAssetWithSettings.BehaviorAssetCalculus> {


	public CalculusBehavior(String id, RedElement parent, BehaviorAssetWithSettings.BehaviorAssetCalculus asset) {
		super(id, parent, asset);
	}


	@Override
	public void onSignalChange(short input, short oldValue, short value) {
		var A = getInputState(0);
		var B = getInputState(1);
		short res = 0;
		switch(asset.operation) {
			case ADD -> res = (short) (A + B);
			case SUBTRACT -> res = (short) (A - B);
			case DIVIDE -> {
				if(B > 0)
					res = (short) (A / B);
			}
			case MULTIPLY -> res = (short) (A * B);
			case MOD -> {
				if(B > 0)
					res = (short) (A % B);
			}
		}
		if(res > MAX)
			res = MAX;
		else if(res < MIN)
			res = MIN;
		setAllOutput(res);
	}

}
