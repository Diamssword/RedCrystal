package com.diamssword.redCrystal.display;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.HashMap;
import java.util.Map;

public class DisplayEntityGroupHolder {
	private final Holder<EntityStore>[] inputs;
	private final Holder<EntityStore>[] outputs;
	private Holder<EntityStore> main;
	private final Map<String, Holder<EntityStore>> others = new HashMap<>();

	public DisplayEntityGroupHolder(short InSize, short OutSize) {
		inputs = new Holder[InSize];
		outputs = new Holder[OutSize];
	}

	public void setOutput(short index, Holder<EntityStore> entity) {
		if(index < outputs.length)
			outputs[index] = entity;
	}

	public void setExtra(String id, Holder<EntityStore> entity) {
		this.others.put(id, entity);
	}

	public void setMain(Holder<EntityStore> entity) {
		this.main = entity;
	}

	public void setInput(short index, Holder<EntityStore> entity) {
		if(index < inputs.length)
			inputs[index] = entity;
	}

	public Holder<EntityStore> getMain() {
		return main;
	}

	public Holder<EntityStore> getOther(String id) {
		return others.get(id);
	}

	public Map<String, Holder<EntityStore>> getOthers() {
		return others;
	}

	public Holder<EntityStore>[] getInputs() {
		return inputs;
	}

	public Holder<EntityStore>[] getOutputs() {
		return outputs;
	}

	public DisplayEntityGroup spawnEntities(Store<EntityStore> store) {
		var res = new DisplayEntityGroup((short) this.inputs.length, (short) this.outputs.length);
		for(short i = 0; i < this.inputs.length; i++) {
			var ho = inputs[i];
			if(ho != null)
				res.setInput(i, store.addEntity(ho, AddReason.SPAWN));
		}
		for(short i = 0; i < this.outputs.length; i++) {
			var ho = outputs[i];
			if(ho != null)
				res.setOutput(i, store.addEntity(ho, AddReason.SPAWN));
		}
		if(this.main != null)
			res.setMain(store.addEntity(this.main, AddReason.SPAWN));
		others.forEach((k, v) -> {
			res.setExtra(k, store.addEntity(v, AddReason.SPAWN));
		});
		return res;
	}
}
