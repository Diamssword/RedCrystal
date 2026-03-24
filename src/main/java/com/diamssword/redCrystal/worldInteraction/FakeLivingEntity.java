package com.diamssword.redCrystal.worldInteraction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FakeLivingEntity extends LivingEntity {
	public static final BuilderCodec<FakeLivingEntity> CODEC = BuilderCodec.builder(FakeLivingEntity.class, FakeLivingEntity::new, LivingEntity.CODEC).build();

	public static ComponentType<EntityStore, FakeLivingEntity> getElementType() {
		return EntityModule.get().getComponentType(FakeLivingEntity.class);
	}

	public FakeLivingEntity() {
		super();
	}

	public FakeLivingEntity(@Nonnull World world) {
		super(world);
	}

	public static class OnFakeLivingAdded extends HolderSystem<EntityStore> {
		public OnFakeLivingAdded() {
		}

		@Override
		public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
			FakeLivingEntity npc = holder.getComponent(FakeLivingEntity.getElementType());

			assert npc != null;

			npc.getInventory().migrateToComponents(holder);
			if(!holder.getArchetype().contains(InventoryComponent.Storage.getComponentType())) {
				holder.addComponent(InventoryComponent.Storage.getComponentType(), new InventoryComponent.Storage((short) 0));
			}

			if(!holder.getArchetype().contains(InventoryComponent.Armor.getComponentType())) {
				holder.addComponent(InventoryComponent.Armor.getComponentType(), new InventoryComponent.Armor(InventoryComponent.DEFAULT_ARMOR_CAPACITY));
			}

			if(!holder.getArchetype().contains(InventoryComponent.Hotbar.getComponentType())) {
				holder.addComponent(InventoryComponent.Hotbar.getComponentType(), new InventoryComponent.Hotbar((short) 3));
			}

			if(!holder.getArchetype().contains(InventoryComponent.Utility.getComponentType())) {
				holder.addComponent(InventoryComponent.Utility.getComponentType(), new InventoryComponent.Utility((short) 0));
			}

			npc.getInventory().backwardsCompatHook(holder);
		}

		@Override
		public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
		}

		@Nullable
		@Override
		public Query<EntityStore> getQuery() {
			return FakeLivingEntity.getElementType();
		}
	}
}
