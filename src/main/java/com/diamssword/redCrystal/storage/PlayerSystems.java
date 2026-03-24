package com.diamssword.redCrystal.storage;

import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.bson.BsonDocument;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

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

	public static class InventoryTicking extends DelayedEntitySystem<EntityStore> {


		public InventoryTicking() {
			super(5);
		}

		@Override
		public Query<EntityStore> getQuery() {
			return Player.getComponentType();
		}

		@Override
		public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

			var player = archetypeChunk.getComponent(index, Player.getComponentType());
			if(player != null) {
				var hotbar = player.getInventory().getHotbar();
				for(short i = 0; i < hotbar.getCapacity(); i++) {
					var stack = hotbar.getItemStack(i);
					if(!ItemStack.isEmpty(stack)) {
						if(stack.getMaxDurability() > 0 && stack.getDurability() < stack.getMaxDurability()) {
							if(stack.getItem() != null && stack.getItem().getData() != null) {
								var tags = stack.getItem().getData().getRawTags().get("RedCrystal");
								if(tags != null) {
									for(String tag : tags) {
										if(tag.equals("Replenishable")) {
											consumeShard(i, player);
											return;

										}
									}
								}
							}
						}
					}
				}
			}
		}

		private void consumeShard(short slot, Player player) {
			var result = player.getInventory().getCombinedBackpackStorageHotbar().removeItemStack(new ItemStack("RedCrystal_Red_Sliver", 1));
			if(result.succeeded()) {
				var st = player.getInventory().getHotbar().getItemStack(slot);
				player.getInventory().getHotbar().replaceItemStackInSlot(slot, st, st.withIncreasedDurability(1));
			}

		}
	}
}
