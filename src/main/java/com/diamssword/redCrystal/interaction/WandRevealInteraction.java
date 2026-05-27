package com.diamssword.redCrystal.interaction;

import com.diamssword.redCrystal.network.NetworkUtil;
import com.diamssword.redCrystal.storage.PlayerDatas;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector3i;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class WandRevealInteraction extends SimpleInteraction {
	public static final BuilderCodec<WandRevealInteraction> CODEC = BuilderCodec.builder(WandRevealInteraction.class, WandRevealInteraction::new, SimpleInteraction.CODEC).build();
	float last = 0;

	void sendUpdateForNewEntities(Ref<EntityStore> playerRef, PlayerDatas playerDatas) {
		var visibleEntities = NetworkUtil.getVisibleEntities(playerRef).stream().filter(e -> !playerDatas.viewedEntities.contains(e)).collect(Collectors.toSet());
		for(Ref<EntityStore> entityRef : visibleEntities) {
			NetworkUtil.setRuneVisibility(entityRef, playerRef, true);
			playerDatas.viewedEntities.add(entityRef);
		}
	}

	void sendUpdateToAllPreviousEntities(Ref<EntityStore> playerRef, PlayerDatas playerDatas) {
		for(Iterator<Ref<EntityStore>> it = playerDatas.viewedEntities.iterator(); it.hasNext(); ) {
			Ref<EntityStore> entityRef = it.next();
			it.remove();
			NetworkUtil.setRuneVisibility(entityRef, playerRef, false);
		}
	}

	@Override
	public void handle(@NonNullDecl Ref<EntityStore> ref, boolean firstRun, float time, @NonNullDecl InteractionType type, @NonNullDecl InteractionContext context) {
		super.handle(ref, firstRun, time, type, context);
		if(context.getState().state != InteractionState.NotFinished) {
			var ent = context.getEntity();
			if(ent != null && ent.isValid() && context.getEntity().getStore().isInThread()) {
				context.getCommandBuffer().run((s) -> {
					var comp = s.ensureAndGetComponent(context.getEntity(), PlayerDatas.getComponentType());
					comp.setToolEquiped(false);
					comp.hideHud();
					sendUpdateToAllPreviousEntities(ref, comp);
				});


			}
		}
	}

	protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
		super.tick0(firstRun, time, type, context, cooldownHandler);

		if(((firstRun || time > last + 0.5) && (context.getState()).state == InteractionState.NotFinished)) {
			last = time;
			Ref<EntityStore> ref = context.getEntity();
			if(ref.isValid()) {
				context.getCommandBuffer().run((s) -> {
					var comp = s.ensureAndGetComponent(context.getEntity(), PlayerDatas.getComponentType());
					sendUpdateForNewEntities(ref, comp);
					if(firstRun) {
						comp.setToolEquiped(true);

						var pref = s.getComponent(context.getEntity(), PlayerRef.getComponentType());
						if(pref != null)
							comp.showHud(pref);
					}
				});

			}

		} else if(!firstRun && context.getState().state != InteractionState.Finished) {

			var pos = TargetUtil.getTargetBlock(context.getEntity(), 5, context.getCommandBuffer());
			var comp = context.getCommandBuffer().getComponent(context.getEntity(), PlayerDatas.getComponentType());
			if(comp != null)
				comp.updateHoveredElement(context.getCommandBuffer().getExternalData().getWorld(), pos, pos == null ? null : getHitFace(pos, TargetUtil.getLook(context.getEntity(), context.getCommandBuffer())));

		}
	}

	public static BlockFace getHitFace(Vector3i blockPos, Transform transform) {
		double tMinX, tMaxX, tMinY, tMaxY, tMinZ, tMaxZ;
		var rayDir = transform.getDirection();
		var rayOrigin = transform.getPosition();
		if(rayDir.x != 0.0) {
			double invX = 1.0 / rayDir.x;
			tMinX = (blockPos.x - rayOrigin.x) * invX;
			tMaxX = (blockPos.x + 1.0 - rayOrigin.x) * invX;
			if(tMinX > tMaxX) {
				tMinX = tMaxX;
			}
		} else {
			tMinX = Double.NEGATIVE_INFINITY;
		}
		if(rayDir.y != 0.0) {
			double invY = 1.0 / rayDir.y;
			tMinY = (blockPos.y - rayOrigin.y) * invY;
			tMaxY = (blockPos.y + 1.0 - rayOrigin.y) * invY;
			if(tMinY > tMaxY) {
				tMinY = tMaxY;
			}
		} else {
			tMinY = Double.NEGATIVE_INFINITY;
		}

		if(rayDir.z != 0.0) {
			double invZ = 1.0 / rayDir.z;
			tMinZ = (blockPos.z - rayOrigin.z) * invZ;
			tMaxZ = (blockPos.z + 1.0 - rayOrigin.z) * invZ;
			if(tMinZ > tMaxZ) {
				tMinZ = tMaxZ;
			}
		} else {
			tMinZ = Double.NEGATIVE_INFINITY;
		}
		double tEnterX = tMinX, tEnterY = tMinY, tEnterZ = tMinZ;
		double tEnter = Math.max(tEnterX, Math.max(tEnterY, tEnterZ));

		if(tEnter == tEnterX) {
			return (rayDir.x > 0) ? BlockFace.West : BlockFace.East;
		} else if(tEnter == tEnterY) {
			return (rayDir.y > 0) ? BlockFace.Down : BlockFace.Up;
		} else {
			return (rayDir.z > 0) ? BlockFace.North : BlockFace.South;
		}
	}
}
