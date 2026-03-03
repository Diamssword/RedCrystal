package com.diamssword.redCrystal.redComponent;

import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.display.RedEntityHiddenComponent;
import com.diamssword.redCrystal.interaction.ToolSettings;
import com.diamssword.redCrystal.redComponent.utils.RedTimers;
import com.diamssword.redCrystal.storage.RedElement;
import com.hypixel.hytale.builtin.buildertools.tooloperations.LaserPointerOperation;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class RedCompBehavior {
	public final short MAX = 255;
	public final short MIN = 0;
	public final RedElement parent;
	public final String id;
	public final short[] state;
	public final short[] stateOutput;
	public final RedTimers timers = new RedTimers();

	public RedCompBehavior(String id, RedElement parent) {
		this.parent = parent;
		this.id = id;
		state = new short[maxInputs()];
		stateOutput = new short[maxOutputs()];
	}

	public abstract short maxInputs();

	public abstract short maxOutputs();

	protected void setInput(short input, short value) {
		if(input < maxInputs()) {
			if(state[input] != value) {
				var old = state[input];
				state[input] = value;
				onSignalChange(input, old, state[input]);
				lightUpForInput(input, value > 0);
			}
		}
	}

	abstract void onSignalChange(short input, short oldValue, short value);

	void setOutput(short output, short value) {
		var chan = parent.getOuput(output);
		if(chan != null) {
			stateOutput[output] = value;
			var behavior = chan.getBehavior(parent.getParent());
			if(behavior != null && behavior.parent.isValid()) {
				behavior.setInput(chan.getInputIndex(), value);
				if(value > 0)
					RedComponentDisplayUtils.drawLaser(getWorld().getEntityStore().getStore(), RedComponentDisplayUtils.getInputPosition(chan.getInputIndex(), behavior), RedComponentDisplayUtils.getOutputPosition(output, this), 0.5f, value);

				lightUpRune(this.parent.getEntities().getOutput(output), value > 0);
			}

		}
	}

	public void setPulse(short output, short onValue, int ticks) {
		setOutput(output, onValue);
		timers.add(() -> setOutput(output, MIN), ticks);
	}

	protected void lightUpForInput(short input, boolean on) {
		lightUpRune(this.parent.getEntities().getInput(input), on);
		lightUpRune(this.parent.getEntities().getMain(), on);
	}


	protected void lightUpRune(Ref<EntityStore> entity, boolean on) {
		if(entity != null) {
			var comp = entity.getStore().getComponent(entity, RedEntityHiddenComponent.getComponentType());
			if(comp != null)
				comp.setLightUp(on);
		}
	}

	public void onEntityInteract(String type, short index, Ref<EntityStore> player, Ref<EntityStore> entity) {
		if(type.equals("input")) {
			getWorld().execute(() -> {
				var comp = player.getStore().ensureAndGetComponent(player, ToolSettings.getComponentType());
				comp.tryToLink(player, this.parent, index, false);
			});

		} else if(type.equals("output")) {
			getWorld().execute(() -> {
				var comp = player.getStore().ensureAndGetComponent(player, ToolSettings.getComponentType());
				comp.tryToLink(player, this.parent, index, true);
			});
		}
	}


	public Map<String, Holder<EntityStore>> displayEntities(EntityStore store, BlockFace facing) {
		return new HashMap<>();
	}

	//TODO might be nice to have a constant time unit between firings
	void runNextTick(Runnable runnable) {
		timers.add(runnable, 1);
		/*World w = getWorld();
		assert w != null;
		w.execute(runnable);*/
	}

	public void displayTick() {
		for(short i = 0; i < stateOutput.length; i++) {
			if(stateOutput[i] > 0) {

				var chan = this.parent.getOuput(i);
				if(chan != null) {
					var bh = chan.getBehavior(this.parent.getParent());
					if(bh != null)
						RedComponentDisplayUtils.drawLaser(getWorld().getEntityStore().getStore(), RedComponentDisplayUtils.getOutputPosition(i, this), RedComponentDisplayUtils.getInputPosition(chan.getInputIndex(), bh), 0.6f, stateOutput[i]);
				}

			}
		}
	}

	public void tick() {
		timers.tick();
	}

	public World getWorld() {
		assert parent.getParent() != null;
		return parent.getParent().getChunkRef().getStore().getExternalData().getWorld();
	}
}
