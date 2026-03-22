package com.diamssword.redCrystal.redComponent;

import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.display.RedEntityHiddenComponent;
import com.diamssword.redCrystal.storage.*;
import com.diamssword.redCrystal.wand.GlyphSettingsMenu;
import com.diamssword.redCrystal.interaction.WandBlockInteraction;
import com.diamssword.redCrystal.redComponent.utils.RedTimers;
import com.diamssword.redCrystal.storage.assets.AbstractBehaviorAsset;
import com.diamssword.redCrystal.wand.RedWandTool;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.BlockSoundEvent;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.server.core.asset.type.blocksound.config.BlockSoundSet;
import com.hypixel.hytale.server.core.asset.type.soundset.config.SoundSet;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RedCompBehavior<T extends AbstractBehaviorAsset<?>> {

	public static final short MAX = 255;
	public static final short MIN = 0;
	public final RedElement parent;
	private final String id;
	private final short[] state;
	private final short[] stateOutput;
	private final Map<String, Short> stateInternal = new HashMap<>();
	public final RedTimers timers = new RedTimers();
	public final T asset;

	public String getId() {
		return id;
	}

	public RedCompBehavior(String id, RedElement parent, T asset) {
		this.parent = parent;
		this.id = id;
		this.asset = asset;
		state = new short[maxInputs()];
		stateOutput = new short[maxOutputs()];
	}

	public short getState(int index) {
		if(index < state.length && index >= 0)
			return state[index];
		return 0;
	}

	public void setState(int index, short value) {
		if(index < state.length && index >= 0)
			state[index] = value;
	}

	public void setInternalState(String id, short state) {
		stateInternal.put(id, state);
	}

	public short getInternalState(String id) {
		return stateInternal.getOrDefault(id, (short) 0);
	}

	public short getStateOutput(int index) {
		if(index < stateOutput.length && index >= 0)
			return stateOutput[index];
		return 0;
	}

	public void setStateOutput(int index, short value) {
		if(index < stateOutput.length && index >= 0)
			stateOutput[index] = value;
	}

	public short maxInputs() {
		return parent.getAsset().getInputs();
	}

	public short maxOutputs() {
		return parent.getAsset().getOutputs();
	}

	public void setInput(short input, short value) {
		if(input < maxInputs()) {
			if(getState(input) != value) {
				var old = getState(input);
				setState(input, value);
				onSignalChange(input, old, getState(input));
				lightUpForInput(input, value > 0);
			}
		}
	}

	abstract void onSignalChange(short input, short oldValue, short value);

	public void setAllOutput(short value) {
		for(int i = 0; i < maxOutputs(); i++) {
			setOutput((short) i, value);
		}
	}

	public void pulseAllOutput(short value, int ticks) {
		for(int i = 0; i < maxOutputs(); i++) {
			setPulse((short) i, value, ticks);
		}
	}

	public void refreshOutputs(short index) {

		setOutput(index, this.stateOutput[index]);
	}

	public void setDefaultOutput(short value) {
		for(int i = 0; i < maxOutputs(); i++) {

			stateOutput[i] = value;
		}
	}

	public void setOutput(short output, short value) {
		var chan = parent.getOuput(output);
		if(chan != null) {
			setStateOutput(output, value);
			var behavior = chan.getBehavior(parent.getParent());
			if(behavior != null && behavior.parent.isValid()) {
				behavior.setInput(chan.getInputIndex(), value);
				if(value > 0 && parent.getSettings().getVisibility() != RedEntityHiddenComponent.Visibility.Invisible)
					RedComponentDisplayUtils.drawLaser(getWorld().getEntityStore().getStore(), RedComponentDisplayUtils.getInputPosition(chan.getInputIndex(), behavior), RedComponentDisplayUtils.getOutputPosition(output, this), 0.5f, value);
				lightUpRune(this.parent.getEntities().getOutput(output), value > 0);
				if(parent.getSettings().getVisibility() == RedEntityHiddenComponent.Visibility.Pulse)
					timers.add(() -> lightUpRune(this.parent.getEntities().getOutput(output), false), 10);
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

	public void onEntityInteract(String type, short index, Ref<EntityStore> player, Ref<EntityStore> entity, InteractionContext context, InteractType action) {
		if(type.equals("input") || type.equals("output")) {
			getWorld().execute(() -> {
				var comp = player.getStore().ensureAndGetComponent(player, PlayerDatas.getComponentType());
				var isOutput = type.equals("output");
				if(action == InteractType.Remove) {
					if(!comp.linkingState.tryCancelLink(this.parent, index, isOutput)) {
						if(isOutput) {
							this.parent.breakOutputNode(index);
						} else {
							var other = this.parent.getInput(index);
							if(other != null && other.isValid()) {
								for(int i = 0; i < other.getAsset().getOutputs(); i++) {
									if(other.getOuput(i).getInputIndex() == index) {
										//TODO issues here
										other.breakOutputNode(i);
										break;
									}
								}
							}
						}
					}
					RedWandTool.playSound("Unselect", parent.getParent().getPosition(), context.getEntity(), getWorld().getEntityStore().getStore());
				} else {
					comp.linkingState.tryToLink(player, this.parent, index, isOutput);
					RedWandTool.playSound("Select", parent.getParent().getPosition(), context.getEntity(), getWorld().getEntityStore().getStore());
				}
			});

		} else if(type.equals("main") && action != InteractType.Remove) {
			onMainRuneInteract(player, entity, context, action);
		} else if(action == InteractType.Remove) {
			WandBlockInteraction.tryRemoveRune(getWorld(), parent.getParent(), parent.getFace(), context);
			if(context.getState().state == InteractionState.Finished) {
				RedWandTool.playSound("Break", parent.getParent().getPosition(), context.getEntity(), getWorld().getEntityStore().getStore());
			}
		}
	}

	public void onMainRuneInteract(Ref<EntityStore> player, @Nullable Ref<EntityStore> entity, InteractionContext context, InteractType action) {
		var pl = player.getStore().getComponent(player, PlayerRef.getComponentType());
		if(pl != null)
			new GlyphSettingsMenu(pl, this.parent::getSettings, this.parent::updateSettings).openMenu();
	}

	public StateLoader getStoredState() {
		return new StateLoader(state, stateOutput, stateInternal);
	}

	public void loadState(StateLoader storedState) {
		//storedState.copyInput(state);
		storedState.copyOutput(stateOutput);
		storedState.copyInternal(stateInternal);
	}

	public enum InteractType {
		Interact,
		Use,
		Remove
	}

	public Map<String, Holder<EntityStore>> displayEntities(EntityStore store, BlockFace facing) {
		return new HashMap<>();
	}

	void runNextTick(Runnable runnable) {
		timers.add(runnable, 1);
	}

	void execute(Runnable runnable) {
		World w = getWorld();
		if(w != null)
			w.execute(runnable);
	}

	public void displayTick() {
		if(parent.getSettings().getVisibility() != RedEntityHiddenComponent.Visibility.Invisible && parent.getSettings().getVisibility() != RedEntityHiddenComponent.Visibility.Pulse) {
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
	}

	public void tick() {
		timers.tick();
	}

	@Nullable
	public World getWorld() {
		if(parent.getParent() != null && parent.getParent().getChunkRef() != null)
			return parent.getParent().getChunkRef().getStore().getExternalData().getWorld();
		return null;
	}

	public List<Short> getOutputValues() {
		List<Short> res = new ArrayList<>();
		for(short i = 0; i < this.maxOutputs(); i++) {
			if(i < stateOutput.length)
				res.add(this.stateOutput[i]);
			else
				res.add((short) 0);
		}
		return res;
	}

	public List<Short> getInputValues() {
		List<Short> res = new ArrayList<>();
		for(short i = 0; i < this.maxInputs(); i++) {
			if(i < state.length)
				res.add(this.state[i]);
			else
				res.add((short) 0);
		}
		return res;
	}

	public List<Short> getConnectedInputs() {
		List<Short> res = new ArrayList<>();
		for(short i = 0; i < this.maxInputs(); i++) {
			var in = this.parent.getInput(i);
			if(in != null && in.isValid())
				res.add(i);
		}
		return res;
	}
}
