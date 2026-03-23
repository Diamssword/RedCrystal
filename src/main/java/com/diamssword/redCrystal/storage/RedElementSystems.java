package com.diamssword.redCrystal.storage;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class RedElementSystems {
	public static class RedElementAddedSystem extends RefSystem<ChunkStore> {

		@Override
		public Query<ChunkStore> getQuery() {
			return Query.and(RedElementState.getComponent(), BlockModule.BlockStateInfo.getComponentType());
		}


		@Override
		public void onEntityAdded(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl AddReason reason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
			RedElementState redComponent = commandBuffer.getComponent(ref, RedElementState.getComponent());
			assert redComponent != null;

			BlockModule.BlockStateInfo blockStateInfoComponent = commandBuffer.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());

			assert blockStateInfoComponent != null;
			WorldChunk worldChunkComponent = commandBuffer.getComponent(blockStateInfoComponent.getChunkRef(), WorldChunk.getComponentType());
			Vector3i blockPosition = new Vector3i(
					ChunkUtil.worldCoordFromLocalCoord(worldChunkComponent.getX(), ChunkUtil.xFromBlockInColumn(blockStateInfoComponent.getIndex())),
					ChunkUtil.yFromBlockInColumn(blockStateInfoComponent.getIndex()),
					ChunkUtil.worldCoordFromLocalCoord(worldChunkComponent.getZ(), ChunkUtil.zFromBlockInColumn(blockStateInfoComponent.getIndex()))
			);

			commandBuffer.run((s) -> redComponent.setPosition(blockPosition, blockStateInfoComponent.getChunkRef()));
		}


		@Override
		public void onEntityRemove(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl RemoveReason reason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> buffer) {
			RedElementState redComponent = buffer.getComponent(ref, RedElementState.getComponent());
			assert redComponent != null;
			if(reason == RemoveReason.REMOVE)
				redComponent.onRemove(buffer);
		}
	}

	public static class RedElementDisplayTickSystem extends DelayedEntitySystem<ChunkStore> {


		public RedElementDisplayTickSystem() {
			super(0.5f);
		}

		@Override
		public void tick(float dt, int index, @NonNullDecl ArchetypeChunk<ChunkStore> archetype, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {

			RedElementState redComponent = archetype.getComponent(index, RedElementState.getComponent());
			assert redComponent != null;
			redComponent.getAllElements().forEach((f, e) -> {
				if(e.isValid())
					e.getBehavior().displayTick();

			});
		}

		@Override
		public Query<ChunkStore> getQuery() {
			return RedElementState.getComponent();
		}
	}

	public static class RedElementTickSystem extends DelayedEntitySystem<ChunkStore> {

		public RedElementTickSystem() {
			super(0.1f);
		}

		@Override
		public void tick(float dt, int index, @NonNullDecl ArchetypeChunk<ChunkStore> archetype, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {

			RedElementState redComponent = archetype.getComponent(index, RedElementState.getComponent());
			assert redComponent != null;
			redComponent.getAllElements().forEach((f, e) -> {
				if(e.isValid())
					e.getBehavior().tick();
			});
		}

		@Override
		public Query<ChunkStore> getQuery() {
			return RedElementState.getComponent();
		}
	}
}
