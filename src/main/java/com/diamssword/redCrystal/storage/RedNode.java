package com.diamssword.redCrystal.storage;

import com.diamssword.redCrystal.redComponent.RedCompBehavior;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockFace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RedNode {

	@Nonnull
	public static BuilderCodec<RedNode> CODEC = BuilderCodec.builder(RedNode.class, RedNode::new)
			.append(new KeyedCodec<>("Position", Vector3i.CODEC), (a, b) -> a.position = b, (a) -> a.position)
			.add()
			.append(new KeyedCodec<>("Target", Codec.SHORT), (a, b) -> a.inputIndex = b, (a) -> a.inputIndex)
			.add()
			.append(new KeyedCodec<>("Face", Codec.STRING), RedNode::setFace, (a) -> (a.getFace() == null ? BlockFace.North : a.getFace()).toString())
			.add()
			.build();
	protected Vector3i position;
	protected BlockFace face;
	protected short inputIndex;

	protected RedNode() {
		this(BlockFace.North, new Vector3i(0, 0, 0));
	}


	public RedNode(BlockFace face, Vector3i position, short inputIndex) {
		this.face = face;
		this.position = position;
		this.inputIndex = inputIndex;
	}

	public RedNode(BlockFace face, Vector3i position) {
		this(face, position, (short) 0);
	}

	public Vector3i getPosition() {
		return position;
	}

	public short getInputIndex() {
		return inputIndex;
	}

	public void setInputIndex(short inputIndex) {
		this.inputIndex = inputIndex;
	}

	public BlockFace getFace() {
		return face;
	}

	public void setFace(String face) {
		try {
			this.face = BlockFace.valueOf(face);
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
			this.face = BlockFace.North;
		}

	}

	public void setFace(BlockFace face) {
		this.face = face;
	}

	public Vector3i getWorldPosition(RedElementState parent) {
		return parent.getPosition().clone().add(this.position);
	}

	@Nullable
	public RedElementState getState(RedElementState known) {
		var worldP = getWorldPosition(known);
		var holder = known.getChunkRef().getStore().getExternalData().getWorld().getBlockComponentHolder(worldP.x, worldP.y, worldP.z);
		if(holder != null) {
			return holder.getComponent(RedElementState.getComponent());
		}
		return null;
	}

	@Nullable
	public RedElement getElement(RedElementState known) {
		var state = getState(known);
		if(state != null) {
			return state.getElement(this.getFace());
		}
		return null;
	}

	@Nullable
	public RedCompBehavior getBehavior(RedElementState known) {
		var state = getState(known);
		if(state != null) {
			var face = state.getElement(this.getFace());
			if(face != null)
				return face.getBehavior();
		}
		return null;
	}
}
