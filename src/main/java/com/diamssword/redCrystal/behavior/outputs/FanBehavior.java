package com.diamssword.redCrystal.behavior.outputs;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.storage.DisplayState;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
import com.diamssword.redCrystal.worldInteraction.FakeLivingEntity;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemPhysicsComponent;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.InteractionSimulationHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsBodyState;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

		/*var min = RedComponentDisplayUtils.getCenteredPosition(parent.getParent().getPosition(), parent.getFace(), new Vector2d(-0.5, -0.5));
		var max = min.clone().add(RedComponentDisplayUtils.rotationoffset(this.parent.getFace(), (maxDistance / (float) MAX) * val, 1, 1));
		var box = new Box(new Vector3d(Math.min(min.x, max.x), Math.min(min.y, max.y), Math.min(min.z, max.z)), new Vector3d(Math.max(min.x, max.x), Math.max(min.y, max.y), Math.max(min.z, max.z)));
*/
		//TODO find the proper methods to find bounding boxes intersecting with the fan
		if(val > MIN) {
			var ents = getEntities(val);
			for(Ref<EntityStore> ent : ents) {
				var vel = ent.getStore().getComponent(ent, Velocity.getComponentType());
				if(vel != null) {
					var dir = RedComponentDisplayUtils.rotationoffset(this.parent.getFace(), 10 * (val / (float) MAX), 0, 0);
					var it = ent.getStore().getComponent(ent, ItemPhysicsComponent.getComponentType());
					if(it != null) {
						var trans = ent.getStore().getComponent(ent, TransformComponent.getComponentType());
						//TODO velocity dosen't work on items
						trans.teleportPosition(trans.getPosition().clone().add(dir.scale(0.1)));
					}
					vel.addInstruction(dir, new VelocityConfig(), ChangeVelocityType.Add);
				}
			}
		}

	}

	public List<Ref<EntityStore>> getEntities(short value) {
		var rot = RedComponentDisplayUtils.rotationoffset(this.parent.getFace(), (maxDistance / (float) MAX) * value, 3, 3);
		var base = RedComponentDisplayUtils.getCenteredPosition(parent.getParent().getPosition(), parent.getFace(), new Vector2d(-2, -2));
		var p1 = base.clone();
		var p2 = base.clone().add(rot.clone());
		//var targ = TargetUtil.getAllEntitiesInSphere(p1, 10, getWorld().getEntityStore().getStore());
		var p3 = new Vector3d(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.min(p1.z, p2.z));
		var p4 = new Vector3d(Math.max(p1.x, p2.x), Math.max(p1.y, p2.y), Math.max(p1.z, p2.z));
		var targ = TargetUtil.getAllEntitiesInBox(p3, p4, getWorld().getEntityStore().getStore());
		SpatialResource<Ref<EntityStore>, EntityStore> entitySpatialResource = getWorld().getEntityStore().getStore().getResource(EntityModule.get().getItemSpatialResourceType());
		entitySpatialResource.getSpatialStructure().collectBox(p3, p4, targ);
		return targ;
		//playerSpatialResource.getSpatialStructure().collectBox(base, base.clone().add(rot), playerRefs);
		//return playerRefs;
	}
}
