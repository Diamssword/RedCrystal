package com.diamssword.redCrystal.behavior;

import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.diamssword.redCrystal.storage.RedElement;

public class AndBehavior extends RedCompBehavior<BehaviorAsset> {


	public AndBehavior(String id, RedElement parent, BehaviorAsset asset) {
		super(id, parent, asset);
	}


	@Override
	void onSignalChange(short input, short oldValue, short value) {
		if(value == MIN) {
			runNextTick(() -> setAllOutput(MIN));
		} else {
			var val = MIN;
			for(Short connectedInput : getConnectedInputs()) {
				var st = getState(connectedInput);
				if(st == MIN) {
					val = MIN;
					break;
				} else if(st > val)
					val = st;
			}
			short finalVal = val;
			runNextTick(() -> setAllOutput(finalVal));
		}

	}

}
