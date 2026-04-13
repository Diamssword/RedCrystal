package com.diamssword.redCrystal.behavior.inputs;

import com.diamssword.redCrystal.behavior.base.RedCompBehaviorWithSettings;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.gui.GlyphSettingsValidators;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.diamssword.redCrystal.wand.LinkingState;
import com.diamssword.redCrystal.worldInteraction.CollideUtil;
import com.diamssword.redCrystal.worldInteraction.FacingUtil;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.raycast.RaycastAABB;
import com.hypixel.hytale.math.shape.Box;
import org.joml.Vector2d;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.collision.CollisionMath;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.util.RayBlockHitTest;
import org.joml.Vector3d;

import java.util.List;

public class LaserDetectorBehavior extends RedCompBehaviorWithSettings<BehaviorAsset, LaserDetectorBehavior.LaserDetectorSettings> {
	public LaserDetectorBehavior(String id, RedElement parent, BehaviorAsset asset) {
		super(id, parent, asset);
	}

	public static BuilderCodec<LaserDetectorBehavior.LaserDetectorSettings> CODEC = BuilderCodec.builder(LaserDetectorBehavior.LaserDetectorSettings.class, LaserDetectorBehavior.LaserDetectorSettings::new)
			.append(new KeyedCodec<>("LaserDetectorBehaviorRange", BuilderCodec.FLOAT), (a, b) -> a.range = b, a -> a.range)
			.addValidator(new GlyphSettingsValidators.SliderRangeValidator<>(0.5f, 16f, 0.5f)).add()
			.append(new KeyedCodec<>("LaserDetectorBehaviorItem", BuilderCodec.BOOLEAN), (a, b) -> a.checkForItem = b, a -> a.checkForItem).add()
			.append(new KeyedCodec<>("LaserDetectorBehaviorBlock", BuilderCodec.BOOLEAN), (a, b) -> a.checkForBlock = b, a -> a.checkForBlock).add()
			.append(new KeyedCodec<>("LaserDetectorBehaviorShow", BuilderCodec.BOOLEAN), (a, b) -> a.showLaser = b, a -> a.showLaser).add().build();


	private boolean active = false;
	private float lastLength = 16f;

	@Override
	public void onSignalChange(short input, short oldValue, short value) {
	}

	@Override
	public void onEntityInteract(String type, short index, Ref<EntityStore> player, Ref<EntityStore> entity, InteractionContext context, InteractType action) {
		super.onEntityInteract(type, index, player, entity, context, action);
		if(type.equals("plate") && action != InteractType.Remove)
			onMainRuneInteract(player, entity, context, action);
	}


	@Override
	public void displayTick() {
		super.displayTick();
		if(getSettings().showLaser) {
			var main = this.parent.getEntities().getMain();
			if(main != null && main.isValid()) {
				var rot = FacingUtil.facingToDir(this.parent.getFace(), lastLength, 0, 0);
				var base = RedComponentDisplayUtils.getCenteredPosition(parent.getParent().getPosition(), parent.getFace(), new Vector2d(0, 0));
				RedComponentDisplayUtils.drawLaserFor(main.getStore(), base, rot.add(base), 0.6f, active ? LinkingState.ERROR_BEAM_COLOR : LinkingState.BASE_BEAM_COLOR, 0.1f);
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		float distance = -1;
		var main = parent.getEntities().getMain();
		if(main != null && main.isValid()) {
			lastLength = getSettings().range;
			var trans = main.getStore().getComponent(main, TransformComponent.getComponentType()).getPosition();
			for(Ref<EntityStore> entity : getEntities()) {

				var trans1 = entity.getStore().getComponent(entity, TransformComponent.getComponentType());
				if(trans1 != null) {
					float dist = (float) Math.abs(trans.distance(trans1.getPosition()));
					if(dist < distance || distance == -1)
						distance = dist;
				}
			}
			var flg = false;
			var bd = CollideUtil.blockDistance(getWorld(), trans, parent.getFace(), getSettings().range);
			if(bd > -1 && (bd < distance || distance == -1)) {
				distance = (float) bd;
				if(!getSettings().checkForBlock)
					flg = true;
			}
			if(distance > -1 && !flg) {
				var scale = 1f - (distance / getSettings().range);
				active = true;
				setAllOutput((short) Math.min(1 + (scale * MAX), MAX));
				lastLength = distance;
			} else {
				if(distance > -1)
					lastLength = distance;
				setAllOutput(MIN);
				active = false;
			}
		}
	}


	public List<Ref<EntityStore>> getEntities() {
		var rot = FacingUtil.facingToDir(this.parent.getFace(), getSettings().range, 0, 0);
		var base = RedComponentDisplayUtils.getCenteredPosition(parent.getParent().getPosition(), parent.getFace(), new Vector2d(-0.01, -0.01));
		var p1 = new Vector3d(base);
		var p2 = new Vector3d(base).add(rot);
		var center = CollideUtil.getBoxCenter(new Box(p1, p2));
		var targ = TargetUtil.getAllEntitiesInSphere(center, getSettings().range * 2, getWorld().getEntityStore().getStore());
		if(getSettings().checkForItem) {
			SpatialResource<Ref<EntityStore>, EntityStore> entitySpatialResource = getWorld().getEntityStore().getStore().getResource(EntityModule.get().getItemSpatialResourceType());
			entitySpatialResource.getSpatialStructure().collect(center, getSettings().range * 2, targ);
		}
		return CollideUtil.filterEntitiesInBox(getWorld().getEntityStore().getStore(), targ, new Box(p1, p2));

	}

	public static class LaserDetectorSettings {
		public float range = 16;
		public boolean showLaser = true;
		public boolean checkForItem = true;
		public boolean checkForBlock = true;

		public LaserDetectorSettings() {

		}
	}
}
