package com.diamssword.redCrystal.behavior.outputs;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.storage.DisplayState;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithInteraction;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
import com.diamssword.redCrystal.worldInteraction.CollideUtil;
import com.diamssword.redCrystal.worldInteraction.FacingUtil;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.damageData.WeaponDamageDataCollector;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemPhysicsComponent;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.InteractionSimulationHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;

public class ProjectileBehavior extends RedCompBehavior<BehaviorAssetWithInteraction> {


	public ProjectileBehavior(String id, RedElement parent, BehaviorAssetWithInteraction asset) {
		super(id, parent, asset);
	}

	public boolean isOnCooldown = false;

	@NullableDecl
	@Override
	public Map<String, Holder<EntityStore>> displayEntities(EntityStore store, BlockFace facing) {
		var res = super.displayEntities(store, facing);
		var holder = RedComponentDisplayUtils.createMinimalDisplayEntity(store, parent.getParent().getPosition(), facing);
		holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(new Box(0, 0, 0, 1, 1, 1)));
		holder.ensureComponent(Intangible.getComponentType());
		holder.putComponent(InteractionModule.get().getInteractionManagerComponent(), new InteractionManager(null, new InteractionSimulationHandler()));
		holder.putComponent(HeadRotation.getComponentType(), new HeadRotation(holder.getComponent(TransformComponent.getComponentType()).getRotation()));
		res.put("interactor", holder);
		return res;
	}

	public void triggerInteraction(InteractionType type) {

		var rootId = asset.interactions.get(type);
		if(rootId != null) {
			RootInteraction root = RootInteraction.getAssetMap().getAsset(rootId);
			if(root != null) {
				var ent = this.parent.getEntities().getOther("interactor");
				if(ent != null && ent.isValid()) {
					var manager = ent.getStore().getComponent(ent, InteractionModule.get().getInteractionManagerComponent());
					InteractionContext ctx = InteractionContext.forInteraction(manager, ent, type, ent.getStore());
					ctx.setInteractionVarsGetter(c -> asset.interactionVars);
					//ctx.getMetaStore().putMetaObject(Interaction.)
					execute(() -> {
						try {
							InteractionChain chain = manager.initChain(type, ctx, root, false);
							manager.queueExecuteChain(chain);
						} catch(Exception e) {
							e.printStackTrace();
						}
					});
				}
			}
		}
	}

	@Override
	public void setLightState(DisplayState display) {
		display.setMain(display.isAnyInputOn());
	}

	@Override
	public void onSignalChange(short input, short oldValue, short value) {
		if(!isOnCooldown) {
			execute(() -> {
				triggerInteraction(value == MIN ? InteractionType.Secondary : InteractionType.Primary);
				isOnCooldown = true;
				timers.add(() -> isOnCooldown = false, (int) (asset.delay * 10));
			});
		}

	}
}
