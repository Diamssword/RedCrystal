package com.diamssword.redCrystal.storage;

import com.hypixel.hytale.builtin.buildertools.tooloperations.LaserPointerOperation;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsSystems;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Set;

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
		public void onEntityRemove(@NonNullDecl Ref<ChunkStore> var1, @NonNullDecl RemoveReason var2, @NonNullDecl Store<ChunkStore> var3, @NonNullDecl CommandBuffer<ChunkStore> var4) {

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
