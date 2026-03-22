package com.diamssword.redCrystal.display;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayEntityGroup {
	private final Ref<EntityStore>[] inputs;
	private final Ref<EntityStore>[] outputs;
	private Ref<EntityStore> main;
	private final Map<String, Ref<EntityStore>> others = new HashMap<>();

	public DisplayEntityGroup(short InSize, short OutSize) {
		inputs = new Ref[InSize];
		outputs = new Ref[OutSize];
	}

	public void setOutput(short index, Ref<EntityStore> entity) {
		if(index < outputs.length && index >= 0)
			outputs[index] = entity;
	}

	public void setExtra(String id, Ref<EntityStore> entity) {
		this.others.put(id, entity);
	}

	public void setMain(Ref<EntityStore> entity) {
		this.main = entity;
	}

	public void setInput(short index, Ref<EntityStore> entity) {
		if(index < inputs.length && index >= 0)
			inputs[index] = entity;
	}

	public Ref<EntityStore> getInput(short index) {
		if(index < inputs.length && index >= 0)
			return inputs[index];
		return null;
	}

	public Ref<EntityStore> getOutput(short index) {
		if(index < outputs.length && index >= 0)
			return outputs[index];
		return null;
	}

	public Ref<EntityStore> getMain() {
		return main;
	}

	public Ref<EntityStore> getOther(String id) {
		return others.get(id);
	}

	public List<Ref<EntityStore>> getAll() {
		var ls = new java.util.ArrayList<>(List.of(inputs));
		ls.addAll(List.of(outputs));
		ls.add(main);
		ls.addAll(others.values());
		return ls;
	}

	public Map<String, Ref<EntityStore>> getOthers() {
		return others;
	}

	public Ref<EntityStore>[] getInputs() {
		return inputs;
	}

	public Ref<EntityStore>[] getOutputs() {
		return outputs;
	}

	public boolean isValid() {
		for(short i = 0; i < this.inputs.length; i++) {
			var ho = inputs[i];
			if(ho != null && !ho.isValid()) {
				return false;
			}
		}
		for(short i = 0; i < this.outputs.length; i++) {
			var ho = outputs[i];
			if(ho != null && !ho.isValid()) {
				return false;
			}
		}
		if(this.main != null && !this.main.isValid()) {
			return false;
		}
		for(Ref<EntityStore> value : others.values()) {
			if(value != null && !value.isValid())
				return false;
		}
		return true;
	}

	public void remove(Store<EntityStore> store) {
		for(short i = 0; i < this.inputs.length; i++) {
			var ho = inputs[i];
			if(ho != null && ho.isValid()) {
				this.setInput(i, null);
				store.removeEntity(ho, RemoveReason.REMOVE);
			}
		}
		for(short i = 0; i < this.outputs.length; i++) {
			var ho = outputs[i];
			if(ho != null && ho.isValid()) {
				this.setOutput(i, null);
				store.removeEntity(ho, RemoveReason.REMOVE);
			}
		}
		if(this.main != null && main.isValid()) {
			store.removeEntity(this.main, RemoveReason.REMOVE);
			this.setMain(null);

		}
		others.forEach((_, v) -> {if(v != null && v.isValid()) {store.removeEntity(v, RemoveReason.REMOVE);}});
		others.clear();
	}
}
