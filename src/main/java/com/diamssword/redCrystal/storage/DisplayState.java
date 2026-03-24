package com.diamssword.redCrystal.storage;

import java.util.Arrays;

public class DisplayState {
	private final Boolean[] inputs;
	private final Boolean[] outputs;
	private boolean main;

	public DisplayState(short sizeIn, short sizeOut) {
		inputs = new Boolean[sizeIn];
		Arrays.fill(inputs, false);
		outputs = new Boolean[sizeOut];
		Arrays.fill(outputs, false);
	}

	public void setOutput(short index, boolean value) {
		if(index < outputs.length)
			outputs[index] = value;
	}

	public void setInput(short index, boolean value) {
		if(index < inputs.length)
			inputs[index] = value;
	}

	public void setAllOutputs(boolean value) {
		Arrays.fill(outputs, value);
	}

	public void setAllInputs(boolean value) {
		Arrays.fill(inputs, value);
	}

	public void setMain(boolean main) {
		this.main = main;
	}

	public Boolean[] getInputs() {
		return inputs;
	}

	public Boolean[] getOutputs() {
		return outputs;
	}

	public boolean getMain() {
		return main;
	}

	public boolean isAnyOutputOn() {
		for(Boolean output : outputs) {
			if(output)
				return true;
		}
		return false;
	}

	public boolean isAnyInputOn() {
		for(Boolean input : inputs) {
			if(input)
				return true;
		}
		return false;
	}
}
