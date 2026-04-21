package com.diamssword.redCrystal.storage;

import com.diamssword.redCrystal.RedCrystalPlugin;
import com.diamssword.redCrystal.display.DisplayEntityGroup;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.wand.RedWandTool;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class RedElement {
	@Nonnull
	public static BuilderCodec<RedElement> CODEC = BuilderCodec.builder(RedElement.class, RedElement::new)
			.append(new KeyedCodec<>("RedElementNodes", new ArrayCodec<>(RedNode.CODEC, RedNode[]::new)), (a, b) -> a.outputs = b, (a) -> a.outputs)
			.add()
			.append(new KeyedCodec<>("RedElementBehavior", Codec.STRING), (a, b) -> a.asset = RedCrystalPlugin.GlyphAssets.getAssetMap().getAsset(b), (a) -> a.asset == null ? null : a.asset.getId())
			.add()
			.append(new KeyedCodec<>("RedElementSettings", GlobalGlyphSettings.CODEC), (a, b) -> a.settings = b, a -> a.settings)
			.add()
			.append(new KeyedCodec<>("RedElementBehaviorState", StateLoader.CODEC), (a, b) -> a.storedState = b, a -> a.storedState)
			.add()
			.build();
	private RedElementState parent;
	private boolean isValid = true;
	private BlockFace face;
	private RedNode[] outputs = new RedNode[0];
	private RedElement[] inputs = new RedElement[0];
	private Glyph asset;
	private RedCompBehavior<?> behavior;
	private GlobalGlyphSettings settings;
	private DisplayEntityGroup linkedEntity;
	private StateLoader storedState;

	public RedElement(RedElementState parent, BlockFace face, @Nullable GlobalGlyphSettings settings) {
		this.settings = settings == null ? new GlobalGlyphSettings() : settings;
		init(parent, face);
	}

	protected RedElement() {}


	public RedElement setAsset(Glyph asset) {
		if(this.linkedEntity != null) {
			var linked = this.linkedEntity;
			var store = parent.getChunkRef().getStore().getExternalData().getWorld().getEntityStore();
			store.getWorld().execute(() -> linked.remove(store.getStore()));
			this.linkedEntity = null;
		}
		this.asset = asset;
		this.behavior = null;
		this.init(parent, face);
		//setupBehavior();
		return this;
	}

	public GlobalGlyphSettings getSettings() {
		return settings;
	}

	public void updateSettings(GlobalGlyphSettings settings) {
		var old = this.settings;
		this.settings = settings;
		old.updateFrom(settings, this);

	}

	public StateLoader getStoredState() {
		return storedState;
	}

	public DisplayEntityGroup getEntities() {

		return linkedEntity == null ? new DisplayEntityGroup(asset.getInputs(), asset.getOutputs()) : linkedEntity;
	}

	public void invalidate() {
		var store = parent.getChunkRef().getStore().getExternalData().getWorld().getEntityStore();
		if(this.linkedEntity != null) {
			store.getWorld().execute(() -> linkedEntity.remove(store.getStore()));
		}
		for(int i = 0; i < this.outputs.length; i++) {
			breakOutputNode(i);
		}
		for(int i = 0; i < this.inputs.length; i++) {
			var in = inputs[i];
			if(in != null) {
				for(int j = 0; j < in.outputs.length; j++) {
					var out = in.getOuput(j);
					if(out != null && out.getInputIndex() == i && out.getCachedElement() == this) {
						in.breakOutputNode(j);
						break;
					}
				}

			}
		}
		this.isValid = false;
	}

	public boolean isValid() {
		return parent != null && isValid && behavior != null && asset != null;
	}

	@Nullable
	public RedCompBehavior<?> getBehavior() {
		return behavior;
	}

	@Nullable
	public Glyph getAsset() {
		return asset;
	}

	private void setupBehavior() {
		if(this.storedState == null)
			this.storedState = new StateLoader(asset.getInputs(), asset.getOutputs());
		else
			this.storedState.updateSize(asset.getInputs(), asset.getOutputs());
		if(asset != null && behavior == null)
			this.behavior = asset.getBehavior().createBehavior(this);
		if(this.behavior != null) {
			var store = parent.getChunkRef().getStore().getExternalData().getWorld().getEntityStore();
			if(linkedEntity == null || !linkedEntity.isValid()) {
				if(linkedEntity != null) {
					var hold = linkedEntity;
					store.getWorld().execute(() -> hold.remove(store.getStore()));
				}
				var holders = RedComponentDisplayUtils.createEditEntities(store, parent.getPosition(), face, this);
				var holdersEx = this.behavior.displayEntities(store, face);
				if(holdersEx != null)
					holders.getOthers().putAll(holdersEx);
				store.getWorld().execute(() -> this.linkedEntity = holders.spawnEntities(store.getStore()));
			}
		}
	}

	public boolean setInput(int index, RedElement element, int outIndex, boolean update) {
		if(behavior != null && index < behavior.InputsCount()) {
			if(index >= inputs.length) {
				inputs = Arrays.copyOf(inputs, index + 1);
			}
			if(inputs[index] != null && inputs[index].isValid() && inputs[index] != element) {
				for(int i = 0; i < inputs[index].outputs.length; i++) {
					var ou = inputs[index].getOuput(i);
					if(ou != null && ou.getInputIndex() == index && ou.getCachedElement() == this) {
						inputs[index].breakOutputNode(i);
						break;
					}
				}
			}
			inputs[index] = element;
			if(this.behavior != null && update)
				this.behavior.setInput((short) index, element.getBehavior().getOutputState(outIndex));
			return true;
		}
		return false;
	}

	private void breakInputInternal(int index) {
		if(index < inputs.length) {
			inputs[index] = null;
			if(this.behavior != null)
				behavior.setInput((short) index, RedCompBehavior.MIN);
		}
	}

	private void breakOutputNodeInternal(int index) {
		if(index < outputs.length) {
			outputs[index] = null;
		}
	}

	public void breakOutputNode(int index) {
		if(index < outputs.length && outputs[index] != null) {
			var el = outputs[index].getElement(this.parent);
			if(el != null && el.isValid()) {
				el.breakInputInternal(outputs[index].inputIndex);
			}
		}
		breakOutputNodeInternal(index);
	}

	public boolean setOutputNode(short index, RedNode node) {
		if(behavior != null && index < behavior.outputsCount()) {
			if(index >= outputs.length) {
				outputs = Arrays.copyOf(outputs, index + 1);
			}
			RedElement el = node.getElement(this.parent);

			if(el != null && el.isValid()) {
				if(!el.setInput(node.inputIndex, this, index, true))
					return false;
			} else
				return false;
			if(outputs[index] != null) {
				var elem = outputs[index].getElement(this.parent);
				//if(elem != null)
				//	elem.setInput(outputs[index].getInputIndex(), null, index, false);
			}
			outputs[index] = node;
			getBehavior().setOutput(index, this.storedState.getOutput()[index]);

			return true;
		}
		return false;
	}

	public RedElement init(RedElementState parent, BlockFace face) {
		this.parent = parent;
		this.face = face;

		if(asset != null) {
			setupBehavior();
			if(this.getBehavior() != null) {
				this.getBehavior().getWorld().execute(() -> {
					for(var i = 0; i < outputs.length; i++) {
						var node = outputs[i];
						if(node != null) {
							RedElement el = node.getElement(this.parent);
							if(el != null && el.isValid()) {
								if(!el.setInput(node.inputIndex, this, i, false)) {
									breakOutputNodeInternal(i);
								} else
									this.behavior.setOutput((short) i, behavior.getOutputState(i));
							}
						}
					}
					this.behavior.timers.add(() -> this.behavior.timers.markLightStateForUpdate(), 2);
				});
			}
		}
		return this;
	}

	public RedElementState getParent() {
		return parent;
	}

	@Nullable
	public RedElement getInput(int index) {
		if(index < inputs.length)
			return inputs[index];
		return null;
	}

	@Nullable
	public RedNode getOuput(int index) {
		if(index < outputs.length)
			return outputs[index];
		return null;
	}

	public BlockFace getFace() {
		return face;
	}

	public void onBreak(BlockFace s, World w, Vector3i pos) {
		if(this.isValid())
			this.invalidate();
		if(w != null)
			w.execute(() -> w.getEntityStore().getStore().addEntity(RedWandTool.dropDust(w.getEntityStore().getStore(), 1, pos, s), AddReason.SPAWN));

	}
}
