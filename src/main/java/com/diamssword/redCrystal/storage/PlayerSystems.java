package com.diamssword.redCrystal.storage;

import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class PlayerSystems {
	public static class ToolTicking extends EntityTickingSystem<EntityStore> {


		@Override
		public Query<EntityStore> getQuery() {
			return PlayerDatas.getComponentType();
		}

		@Override
		public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
			var tool = archetypeChunk.getComponent(index, PlayerDatas.getComponentType());
			var trans = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
			if(tool != null && trans != null) {
				if(tool.isToolEquiped() && tool.linkingState.startedLink != null) {
					var model = archetypeChunk.getComponent(index, ModelComponent.getComponentType());
					var vec1 = RedComponentDisplayUtils.getIOPosition(tool.linkingState.startedLink.index, tool.linkingState.startedLink.source.getBehavior(), tool.linkingState.startedLink.output);
					var vec2 = trans.getPosition().clone();
					if(model != null) {
						vec2.add(0, model.getModel().getBoundingBox().height() / 2, 0);
					}
					tool.linkingState.handleBlink(trans.getPosition());
					RedComponentDisplayUtils.drawLaserFor(vec1, vec2, 0.1f, tool.linkingState.getColor(), archetypeChunk.getReferenceTo(index));
				}
			}
		}
	}
}
