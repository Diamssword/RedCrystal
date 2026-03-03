package com.diamssword.redCrystal.storage;

import com.diamssword.redCrystal.display.DisplayEntityGroup;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.redComponent.RedCompBehavior;
import com.diamssword.redCrystal.redComponent.RedComponentRegister;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.protocol.BlockFace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class RedElement {
	@Nonnull
	public static BuilderCodec<RedElement> CODEC = BuilderCodec.builder(RedElement.class, RedElement::new)
			.append(new KeyedCodec<>("Nodes", new ArrayCodec<>(RedNode.CODEC, RedNode[]::new)), (a, b) -> a.outputs = b, (a) -> a.outputs)
			.add()
			.append(new KeyedCodec<>("Behavior", Codec.STRING), (a, b) -> a.behavior = RedComponentRegister.get(b, a), (a) -> a.behavior == null ? "" : a.behavior.id)
			.add()
			.build();
	private RedElementState parent;
	private boolean isValid = true;
	private BlockFace face;
	private RedNode[] outputs = new RedNode[0];
	private RedElement[] inputs = new RedElement[0];
	private RedCompBehavior behavior;
	private DisplayEntityGroup linkedEntity;

	public RedElement(RedElementState parent, BlockFace face) {
		init(parent, face);
	}

	protected RedElement() {}

	public RedElement setBehavior(RedCompBehavior behavior) {
		if(this.linkedEntity != null) {
			var linked = this.linkedEntity;
			var store = parent.getChunkRef().getStore().getExternalData().getWorld().getEntityStore();
			store.getWorld().execute(() -> linked.remove(store.getStore()));
			this.linkedEntity = null;
		}
		this.behavior = behavior;
		setupBehavior();
		return this;
	}

	@Nullable
	public DisplayEntityGroup getEntities() {
		return linkedEntity;
	}

	public void invalidate() {
		var store = parent.getChunkRef().getStore().getExternalData().getWorld().getEntityStore();
		if(this.linkedEntity != null) {
			store.getWorld().execute(() -> linkedEntity.remove(store.getStore()));
		}
		this.isValid = false;
	}

	public boolean isValid() {
		return parent != null && isValid;
	}

	public RedCompBehavior getBehavior() {
		return behavior;
	}

	private void setupBehavior() {
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
		if(outputs[index] != null) {
			var el = outputs[index].getElement(this.parent);
			if(el != null && el.isValid()) {
				el.breakInputInternal(outputs[index].inputIndex);
			}
		}
		breakOutputNodeInternal(index);
	}

	public boolean setOutputNode(int index, RedNode node) {
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
					if(!el.setInput(node.inputIndex, this))
						breakOutputNodeInternal(i);
				}
			}
		}
		return this;
	}

	public RedElementState getParent() {
		return parent;
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
}
