package com.diamssword.redCrystal.storage;

import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.interaction.ToolSettings;
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
			return ToolSettings.getComponentType();
		}

		@Override
		public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
			var tool = archetypeChunk.getComponent(index, ToolSettings.getComponentType());
			var trans = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
			var model = archetypeChunk.getComponent(index, ModelComponent.getComponentType());
			if(tool != null && trans != null) {
				if(tool.startedLink != null) {
					var vec1 = RedComponentDisplayUtils.getIOPosition(tool.startedLink.index, tool.startedLink.source.getBehavior(), tool.startedLink.output);
					var vec2 = trans.getPosition().clone();
					if(model != null) {
						vec2.add(0, model.getModel().getBoundingBox().height() / 2, 0);
					}
					RedComponentDisplayUtils.drawLaserFor(vec1, vec2, 0.1f, 0x5050FF, archetypeChunk.getReferenceTo(index));
				}
			}
		}
	}
}
