package com.diamssword.redCrystal.behavior.outputs;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.storage.DisplayState;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
import com.diamssword.redCrystal.worldInteraction.CollideUtil;
import com.diamssword.redCrystal.worldInteraction.FacingUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.shape.Box;
import org.joml.Vector2d;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemPhysicsComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;

import java.util.List;

public class FanBehavior extends RedCompBehavior<BehaviorAssetWithSettings.BehaviorAssetDistance> {

	private final float maxDistance;

	public FanBehavior(String id, RedElement parent, BehaviorAssetWithSettings.BehaviorAssetDistance asset) {
		super(id, parent, asset);
		this.maxDistance = asset.distance;
	}

	@Override
	public void setLightState(DisplayState display) {
		display.setMain(display.isAnyInputOn());
	}

	@Override
	public void onSignalChange(short input, short oldValue, short value) {


	}

	@Override
	public void tick() {
		super.tick();
		var val = getInputState(0);
		if(val > MIN) {
			var ents = getEntities(val);
			for(Ref<EntityStore> ent : ents) {
				var vel = ent.getStore().getComponent(ent, Velocity.getComponentType());
				if(vel != null) {
					var dir = FacingUtil.facingToDir(this.parent.getFace(), 10 * (val / (float) MAX), 0, 0);
					var it = ent.getStore().getComponent(ent, ItemPhysicsComponent.getComponentType());
					if(it != null) {
						var trans = ent.getStore().getComponent(ent, TransformComponent.getComponentType());
						if(trans != null) {
							//item physic completely stop item when it's on top of a block, we prevent that by moving in up a bit if it is resting on a block
							if(it.collisionResult != null)
								trans.teleportPosition(new Vector3d(trans.getPosition()).add(0, 0.1, 0));
						}
					}
					dir.add(0, 0.2, 0);
					vel.addInstruction(dir, null, ChangeVelocityType.Set);

				}
			}
		}

	}

	public List<Ref<EntityStore>> getEntities(short value) {
		var rot = FacingUtil.facingToDir(this.parent.getFace(), (maxDistance / (float) MAX) * value, 1, 1);
		var base = RedComponentDisplayUtils.getCenteredPosition(parent.getParent().getPosition(), parent.getFace(), new Vector2d(-0.5, -0.5));
		var p1 = new Vector3d(base);
		var p2 = new Vector3d(base).add(rot);
		var center = CollideUtil.getBoxCenter(new Box(p1, p2));
		var targ = TargetUtil.getAllEntitiesInSphere(center, maxDistance * 2, getWorld().getEntityStore().getStore());
		SpatialResource<Ref<EntityStore>, EntityStore> entitySpatialResource = getWorld().getEntityStore().getStore().getResource(EntityModule.get().getItemSpatialResourceType());
		entitySpatialResource.getSpatialStructure().collect(center, maxDistance * 2, targ);
		return CollideUtil.filterEntitiesInBox(getWorld().getEntityStore().getStore(), targ, new Box(p1, p2));

	}
}
