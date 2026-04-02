package com.diamssword.redCrystal.behavior.inputs;

import com.diamssword.redCrystal.behavior.base.RedCompBehaviorWithSettings;
import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithModelSwitch;
import com.diamssword.redCrystal.worldInteraction.CollideUtil;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.List;
import java.util.Map;

public class PressurePlateBehavior extends RedCompBehaviorWithSettings<BehaviorAssetWithModelSwitch, PressurePlateBehavior.PressurePlateSettings> {
	public PressurePlateBehavior(String id, RedElement parent, BehaviorAssetWithModelSwitch asset) {
		super(id, parent, asset);
		this.setSettingsChangeListener(_ -> onSettingsChange());
	}

	public static BuilderCodec<PressurePlateBehavior.PressurePlateSettings> CODEC = BuilderCodec.builder(PressurePlateBehavior.PressurePlateSettings.class, PressurePlateBehavior.PressurePlateSettings::new)
			.append(new KeyedCodec<>("PressurePlateBehaviorItemCheck", BuilderCodec.BOOLEAN), (a, b) -> a.checkForItem = b, a -> a.checkForItem)
			.add()
			.append(new KeyedCodec<>("PressurePlateBehaviorHidePlate", BuilderCodec.BOOLEAN), (a, b) -> a.hidePlate = b, a -> a.hidePlate)
			.add().build();

	private boolean pressed = false;

	@Override
	public void onSignalChange(short input, short oldValue, short value) {
	}

	@Override
	public void onEntityInteract(String type, short index, Ref<EntityStore> player, Ref<EntityStore> entity, InteractionContext context, InteractType action) {
		super.onEntityInteract(type, index, player, entity, context, action);
		if(type.equals("plate") && action != InteractType.Remove)
			onMainRuneInteract(player, entity, context, action);
	}

	private void onSettingsChange() {
		if(parent.getEntities() != null) {
			var plate = parent.getEntities().getOther("plate");
			if(plate != null) {
				asset.switchModel(this, plate, this.pressed, 0.8f, getSettings().hidePlate);
			}

		}
	}

	@Override
	public void tick() {
		super.tick();
		if(!pressed) {
			var plate = parent.getEntities().getOther("plate");
			if(plate != null && plate.isValid()) {
				var bb = plate.getStore().getComponent(plate, BoundingBox.getComponentType());
				var trans = plate.getStore().getComponent(plate, TransformComponent.getComponentType());
				if(bb != null && trans != null) {
					pressed = false;
					for(Ref<EntityStore> entity : getEntities(trans.getPosition(), bb)) {
						if(entity != plate) {
							pressed = true;
							break;

						}

					}
					if(pressed)
						timers.add(() -> pressed = false, 5);
					setAllOutput(pressed ? MAX : MIN);
					if(!getSettings().hidePlate)
						asset.switchModel(this, plate, pressed, 0.8f);
				}
			}
		}
	}

	public List<Ref<EntityStore>> getEntities(Vector3d pos, BoundingBox box) {
		var targ = TargetUtil.getAllEntitiesInSphere(pos, 5, getWorld().getEntityStore().getStore());
		if(getSettings().checkForItem) {
			SpatialResource<Ref<EntityStore>, EntityStore> entitySpatialResource = getWorld().getEntityStore().getStore().getResource(EntityModule.get().getItemSpatialResourceType());
			entitySpatialResource.getSpatialStructure().collect(pos, 5, targ);
		}
		return CollideUtil.filterEntitiesInBox(getWorld().getEntityStore().getStore(), targ, pos, box.getBoundingBox());

	}

	@NullableDecl
	@Override
	public Map<String, Holder<EntityStore>> displayEntities(EntityStore store, BlockFace facing) {
		var res = super.displayEntities(store, facing);
		var holder = this.asset.createEntity(store, this, false, 0.8f, this.getSettings().hidePlate);

		holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent("plate", (short) 0, this.parent));
		res.put("plate", holder);
		return res;
	}

	public static class PressurePlateSettings {
		public boolean checkForItem = true;
		public boolean hidePlate = false;

		public PressurePlateSettings() {

		}
	}
}
