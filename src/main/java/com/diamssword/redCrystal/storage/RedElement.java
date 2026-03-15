package com.diamssword.redCrystal.storage;

import com.diamssword.redCrystal.RedCrystalPlugin;
import com.diamssword.redCrystal.display.DisplayEntityGroup;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.redComponent.RedCompBehavior;
import com.diamssword.redCrystal.wand.RedWandTool;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class RedElement {
	@Nonnull
	public static BuilderCodec<RedElement> CODEC = BuilderCodec.builder(RedElement.class, RedElement::new)
			.append(new KeyedCodec<>("Nodes", new ArrayCodec<>(RedNode.CODEC, RedNode[]::new)), (a, b) -> a.outputs = b, (a) -> a.outputs)
			.add()
			.append(new KeyedCodec<>("Behavior", Codec.STRING), (a, b) -> a.asset = RedCrystalPlugin.GlyphAssets.getAssetMap().getAsset(b), (a) -> a.asset == null ? null : a.asset.getId())
			.add()
			.append(new KeyedCodec<>("Settings", GlobalGlyphSettings.CODEC), (a, b) -> a.settings = b, a -> a.settings)
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
		setupBehavior();
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

	@Nullable
	public DisplayEntityGroup getEntities() {

		return linkedEntity == null ? new DisplayEntityGroup(asset.getInputs(), asset.getOutputs()) : linkedEntity;
	}

	public void invalidate() {
		var store = parent.getChunkRef().getStore().getExternalData().getWorld().getEntityStore();
		if(this.linkedEntity != null) {
			store.getWorld().execute(() -> linkedEntity.remove(store.getStore()));
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
			store.getWorld().execute(() -> {
				for(short i = 0; i < outputs.length; i++) {
					if(outputs[i] != null)
						behavior.setOutput(i, getBehavior().defaultOutputValue(i));
				}

			});
		}
	}

	public boolean setInput(int index, RedElement element) {
		if(behavior != null && index < behavior.maxInputs()) {
			if(index >= inputs.length) {
				inputs = Arrays.copyOf(inputs, index + 1);
			}
			if(inputs[index] != null && inputs[index].isValid() && inputs[index] != element) {
				inputs[index].breakOutputNodeInternal(index);
			}
			inputs[index] = element;
			return true;
		}
		return false;
	}

	private void breakInputInternal(int index) {
		if(index < inputs.length)
			inputs[index] = null;
	}

	private void breakOutputNodeInternal(int index) {
		if(index < outputs.length)
			outputs[index] = null;
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
		if(behavior != null && index < behavior.maxOutputs()) {
			if(index >= outputs.length) {
				outputs = Arrays.copyOf(outputs, index + 1);
			}
			RedElement el = node.getElement(this.parent);
			if(el != null && el.isValid()) {
				if(!el.setInput(node.inputIndex, this))
					return false;
			} else
				return false;

			outputs[index] = node;
			getBehavior().setOutput(index, getBehavior().defaultOutputValue(index));

			return true;
		}
		return false;
	}

	public RedElement init(RedElementState parent, BlockFace face) {
		this.parent = parent;
		this.face = face;
		setupBehavior();
		for(var i = 0; i < outputs.length; i++) {
			var node = outputs[i];
			if(node != null) {
				RedElement el = node.getElement(this.parent);
				if(el != null && el.isValid()) {
					if(!el.setInput(node.inputIndex, this)) {
						breakOutputNodeInternal(i);
					}
				}
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

	public void onBreak(BlockFace s, CommandBuffer<ChunkStore> buffer) {
		var world = parent.getChunkRef().getStore().getExternalData().getWorld();
		if(world != null)
			world.execute(() -> world.getEntityStore().getStore().addEntity(RedWandTool.dropDust(world.getEntityStore().getStore(), 1, parent.getPosition(), face), AddReason.SPAWN));
		
	}
}
