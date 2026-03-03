package com.diamssword.redCrystal.worldInteraction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

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

	@Override
	protected Inventory createDefaultInventory() {
		return new Inventory((short) 0, (short) 0, (short) 1, (short) 0, (short) 0);
	}
}
