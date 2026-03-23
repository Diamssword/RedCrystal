package com.diamssword.redCrystal.behavior;

import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.display.RedEntityHiddenComponent;
import com.diamssword.redCrystal.storage.*;
import com.diamssword.redCrystal.wand.GlyphSettingsMenu;
import com.diamssword.redCrystal.interaction.WandBlockInteraction;
import com.diamssword.redCrystal.behavior.utils.RedTimers;
import com.diamssword.redCrystal.storage.assets.AbstractBehaviorAsset;
import com.diamssword.redCrystal.wand.RedWandTool;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
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
	public final RedTimers timers = new RedTimers();
	public final T asset;

	public String getId() {
		return id;
	}

	public RedCompBehavior(String id, RedElement parent, T asset) {
		this.parent = parent;
		this.id = id;
		this.asset = asset;
		//	state = new short[maxInputs()];
		//	stateOutput = new short[maxOutputs()];
	}

	private StateLoader getStateManager() {
		return parent.getStoredState();
	}

	public short getState(int index) {
		if(index < getStateManager().getInput().length && index >= 0)
			return getStateManager().getInput()[index];
		return 0;
	}

	public void setState(int index, short value) {
		if(index < getStateManager().getInput().length && index >= 0)
			getStateManager().getInput()[index] = value;
	}

	public void setInternalState(String id, short state) {
		getStateManager().getInternalStates().put(id, state);
	}

	public short getInternalState(String id) {
		return getStateManager().getInternalStates().getOrDefault(id, (short) 0);
	}

	public short getStateOutput(int index) {
		if(index < getStateManager().getOutput().length && index >= 0)
			return getStateManager().getOutput()[index];
		return 0;
	}

	public void setStateOutput(int index, short value) {
		if(index < getStateManager().getOutput().length && index >= 0)
			getStateManager().getOutput()[index] = value;
	}

	public short maxInputs() {
		return parent.getAsset().getInputs();
	}

	public short maxOutputs() {
		return parent.getAsset().getOutputs();
	}

	public void setInput(short input, short value) {
		setInput(input, value, false);
	}

	public void setInput(short input, short value, boolean forceUpdate) {
		if(input < maxInputs()) {
			if(forceUpdate || getState(input) != value) {
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

		setOutput(index, this.getStateManager().getOutput()[index]);
	}

	public void setDefaultOutput(short value) {
		for(int i = 0; i < maxOutputs(); i++) {

			getStateManager().getOutput()[i] = value;
		}
	}

	public void setOutput(short output, short value) {
		setOutput(output, value, false);
	}

	public void setOutput(short output, short value, boolean forceUpdate) {
		var chan = parent.getOuput(output);
		if(chan != null) {
			setStateOutput(output, value);
			var behavior = chan.getBehavior(parent.getParent());
			if(behavior != null && behavior.parent.isValid()) {
				behavior.setInput(chan.getInputIndex(), value, forceUpdate);
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

	public void loadState() {
		//storedState.copyInput(state);
		System.out.println("--loading--");
		getStateManager().getInternalStates().forEach((k, v) -> {
			System.out.println(k + "=" + v);
		});
		runNextTick(() -> {
			var vals = this.getOutputValues();
			for(int i = 0; i < vals.size(); i++) {
				setOutput((short) i, vals.get(i), true);
			}
		});


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
			for(short i = 0; i < getStateManager().getOutput().length; i++) {
				if(getStateManager().getOutput()[i] > 0) {

					var chan = this.parent.getOuput(i);
					if(chan != null) {
						var bh = chan.getBehavior(this.parent.getParent());
						if(bh != null)
							RedComponentDisplayUtils.drawLaser(getWorld().getEntityStore().getStore(), RedComponentDisplayUtils.getOutputPosition(i, this), RedComponentDisplayUtils.getInputPosition(chan.getInputIndex(), bh), 0.6f, getStateManager().getOutput()[i]);
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
			if(i < getStateManager().getOutput().length)
				res.add(this.getStateManager().getOutput()[i]);
			else
				res.add((short) 0);
		}
		return res;
	}

	public List<Short> getInputValues() {
		List<Short> res = new ArrayList<>();
		for(short i = 0; i < this.maxInputs(); i++) {
			if(i < getStateManager().getInput().length)
				res.add(this.getStateManager().getInput()[i]);
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
