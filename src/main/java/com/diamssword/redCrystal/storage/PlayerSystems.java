package com.diamssword.redCrystal.storage;

import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.wand.LinkingState;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class PlayerSystems {
	public static class ToolTicking extends EntityTickingSystem<EntityStore> {


		@Override
		public Query<EntityStore> getQuery() {
			return LinkingState.getComponentType();
		}

		@Override
		public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
			var tool = archetypeChunk.getComponent(index, LinkingState.getComponentType());
			var trans = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
			if(tool != null && trans != null) {
				if(tool.isToolEquiped() && tool.startedLink != null) {
					var model = archetypeChunk.getComponent(index, ModelComponent.getComponentType());
					var vec1 = RedComponentDisplayUtils.getIOPosition(tool.startedLink.index, tool.startedLink.source.getBehavior(), tool.startedLink.output);
					var vec2 = trans.getPosition().clone();
					if(model != null) {
						vec2.add(0, model.getModel().getBoundingBox().height() / 2, 0);
					}
					tool.handleBlink(trans.getPosition());
					RedComponentDisplayUtils.drawLaserFor(vec1, vec2, 0.1f, tool.getColor(), archetypeChunk.getReferenceTo(index));
				}
			}
		}
	}
}
