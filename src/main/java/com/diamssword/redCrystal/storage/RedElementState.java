package com.diamssword.redCrystal.storage;

import com.diamssword.redCrystal.RedCrystalPlugin;
import com.diamssword.redCrystal.redComponent.RedComponentRegister;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class RedElementState implements Component<ChunkStore> {
	@Nonnull
	public static BuilderCodec<RedElementState> CODEC = BuilderCodec.builder(RedElementState.class, RedElementState::new/*, BlockState.BASE_CODEC*/)
			.append(
					new KeyedCodec<>("Elements", new MapCodec<>(RedElement.CODEC, HashMap::new, false)),
					(block, map) -> block.elements = map,
					block -> block.elements
			)
			.add()
			.build();

	public RedElementState() {

	}

	public static ComponentType<ChunkStore, RedElementState> getComponent() {
		return RedCrystalPlugin.RedElementComponent;
	}

	protected Map<String, RedElement> elements = new HashMap<>();
	private Vector3i position;
	private Ref<ChunkStore> chunkRef;

	@NullableDecl
	@Override
	public Component<ChunkStore> clone() {
		var res = new RedElementState();
		res.elements = this.elements;
		res.position = this.position;
		res.chunkRef = this.chunkRef;
		return res;
	}

	public void setPosition(Vector3i position, Ref<ChunkStore> chunkRef) {
		this.position = position;
		this.chunkRef = chunkRef;

		for(var entry : this.getAllElements().entrySet()) {
			if(entry.getValue().getBehavior() == null)
				this.elements.remove(entry.getKey().toString());
			else
				entry.getValue().init(this, entry.getKey());
		}

	}


	@Nullable
	public Vector3i getPosition() {
		return position;
	}

	@Nullable
	public Ref<ChunkStore> getChunkRef() {
		return chunkRef;
	}

	@Nullable
	public RedElement getElement(BlockFace face) {

		return elements.get(face.toString());
	}

	public boolean createElement(BlockFace face, String behaviorId) {
		var res = this.elements.get(face.toString());
		if(res == null) {
			res = new RedElement(this, face);
			var beh = RedComponentRegister.get(behaviorId, res);
			if(beh != null) {
				res.setBehavior(beh);
				this.elements.put(face.toString(), res);
				return true;
			} else
				return false;
		}
		return false;
	}

	public Map<BlockFace, RedElement> getAllElements() {
		var m = new HashMap<BlockFace, RedElement>();
		this.elements.forEach((a, b) -> {
			try {
				m.put(BlockFace.valueOf(a), b);
			} catch(IllegalArgumentException e) {e.printStackTrace();}
		});
		return m;
	}

	public void setElements(BlockFace face, RedElement element) {
		this.elements.put(face.toString(), element);
	}

	public RedElement removeElement(BlockFace blockFace) {
		var el = this.elements.remove(blockFace.toString());
		if(el != null)
			el.invalidate();
		return el;
	}
}
