package com.diamssword.redCrystal.storage;

import com.diamssword.redCrystal.display.DisplayEntityGroup;
import com.diamssword.redCrystal.display.RedEntityHiddenComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

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

	protected void lightUpRune(Ref<EntityStore> entity, boolean on) {
		if(entity != null && entity.isValid()) {
			var comp = entity.getStore().getComponent(entity, RedEntityHiddenComponent.getComponentType());
			if(comp != null)
				comp.setLightUp(on);
		}
	}

	public void updateEntities(DisplayEntityGroup entities) {
		lightUpRune(entities.getMain(), getMain());
		for(int i = 0; i < getInputs().length; i++) {
			lightUpRune(entities.getInput((short) i), getInputs()[i]);
		}
		for(int i = 0; i < getOutputs().length; i++) {
			lightUpRune(entities.getOutput((short) i), getOutputs()[i]);
		}
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

	public void setAll(boolean b) {
		this.setMain(b);
		this.setAllInputs(b);
		setAllOutputs(b);
	}
}
