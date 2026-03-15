package com.diamssword.redCrystal.interaction;

import com.diamssword.redCrystal.display.AttachmentsHelper;
import com.diamssword.redCrystal.display.ModelUtils;
import com.diamssword.redCrystal.display.RedEntityHiddenComponent;
import com.diamssword.redCrystal.network.NetworkUtil;
import com.diamssword.redCrystal.storage.PlayerDatas;
import com.diamssword.redCrystal.wand.LinkingState;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.entities.EntityUpdates;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.collision.EntityRefCollisionProvider;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.receiver.IPacketReceiver;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.PositionUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WandRevealInteraction extends SimpleInteraction {
	public static final BuilderCodec<WandRevealInteraction> CODEC = BuilderCodec.builder(WandRevealInteraction.class, WandRevealInteraction::new, SimpleInteraction.CODEC)

			.build();


	Ref<EntityStore> targetedEntity;
	float last = 0;

	void sendUpdateForNewEntities(Ref<EntityStore> playerRef, PlayerDatas playerDatas) {
		var visibleEntities = NetworkUtil.getVisibleEntities(playerRef).stream().filter(e -> !playerDatas.viewedEntities.contains(e)).collect(Collectors.toSet());

		for(Ref<EntityStore> entityRef : visibleEntities) {
			if(entityRef.isValid()) {
				var hidd = entityRef.getStore().getComponent(entityRef, RedEntityHiddenComponent.getComponentType());
				if(hidd != null) {
					hidd.addSeeingPlayer(playerRef);
					playerDatas.viewedEntities.add(entityRef);
					try {
						ModelComponent modelComponent = entityRef.getStore().getComponent(entityRef, ModelComponent.getComponentType());
						if(modelComponent == null)
							continue;
						var hasInteract = entityRef.getStore().getComponent(entityRef, Interactable.getComponentType()) != null;
						Model model;
						if(hasInteract)
							model = ModelUtils.withAttachment(modelComponent.getModel(), new ModelAttachment("Items/RedCrystal/Glyphs/HitboxHighlight.blockymodel", "Items/RedCrystal/Glyphs/HitboxHighlight.png", null, null, 1));
						else
							model = new Model(modelComponent.getModel());

						ModelUpdate update = new ModelUpdate();
						update.model = model.toPacket();
						update.entityScale = hidd.getVisibleScale();
						NetworkUtil.sendEntityComponentUpdateToPlayer(playerRef, entityRef, new ComponentUpdateType[]{ComponentUpdateType.Intangible}, new ComponentUpdate[]{update});
					} catch(Exception exception) {}
				}
			}
		}
	}

	void sendUpdateToAllPreviousEntities(Ref<EntityStore> playerRef, PlayerDatas playerDatas) {
		for(Iterator<Ref<EntityStore>> it = playerDatas.viewedEntities.iterator(); it.hasNext(); ) {
			Ref<EntityStore> entityRef = it.next();
			it.remove();
			if(entityRef.isValid() && entityRef.getStore().isInThread()) {
				var hidd = entityRef.getStore().getComponent(entityRef, RedEntityHiddenComponent.getComponentType());
				if(hidd != null)
					hidd.removeSeeingPlayer(playerRef);
				ModelComponent modelComponent = entityRef.getStore().getComponent(entityRef, ModelComponent.getComponentType());
				if(modelComponent == null)
					continue;
				Model model = ModelUtils.withAttachment(modelComponent.getModel());
				ModelUpdate update = new ModelUpdate();
				update.model = model.toPacket();

				update.entityScale = hidd.getHiddenScale();
				NetworkUtil.sendEntityComponentUpdateToPlayer(playerRef, entityRef, null, new ComponentUpdate[]{update, new IntangibleUpdate()});
			}
		}
	}

	void higlightEntity(Ref<EntityStore> playerRef, Ref<EntityStore> entityRef) {
		if(targetedEntity != null && targetedEntity.isValid()) {
			ModelComponent modelComponent = targetedEntity.getStore().getComponent(targetedEntity, ModelComponent.getComponentType());
			if(modelComponent != null) {
				Model model = ModelUtils.withAttachment(modelComponent.getModel());
				ModelUpdate update = new ModelUpdate();
				update.model = model.toPacket();
				NetworkUtil.sendEntityComponentUpdateToPlayer(playerRef, targetedEntity, new ComponentUpdateType[]{ComponentUpdateType.Intangible}, new ComponentUpdate[]{update});
			}
		}
		targetedEntity = entityRef;
		ModelComponent modelComponent = entityRef.getStore().getComponent(entityRef, ModelComponent.getComponentType());
		if(modelComponent != null) {
			Model model = ModelUtils.withAttachment(modelComponent.getModel(), new ModelAttachment("Items/RedCrystal/Glyphs/HitboxHighlight.blockymodel", "Items/RedCrystal/Glyphs/HitboxHighlight.png", null, null, 1));
			ModelUpdate update = new ModelUpdate();
			update.model = model.toPacket();
			NetworkUtil.sendEntityComponentUpdateToPlayer(playerRef, entityRef, new ComponentUpdateType[]{ComponentUpdateType.Intangible}, new ComponentUpdate[]{update});
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
					sendUpdateToAllPreviousEntities(ref, comp);
				/*	var skp = s.getComponent(context.getEntity(), PlayerSkinComponent.getComponentType());
					if(skp != null)
						skp.setNetworkOutdated();

				 */
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
					/*	var mod = s.getComponent(context.getEntity(), ModelComponent.getComponentType());
						if(mod != null) {
							var skp = s.getComponent(context.getEntity(), PlayerSkinComponent.getComponentType());
							var mod1 = CosmeticsModule.get().createModel(skp.getPlayerSkin());
							var arr = Arrays.copyOf(mod1.getAttachments(), mod1.getAttachments().length + 1);
							var attachments = AttachmentsHelper.parseSkin(skp.getPlayerSkin(), null, mod.getModel().getGradientId());
							attachments.add(new ModelAttachment("Items/Iron_Offhand.blockymodel", "Items/Weapons/Dagger/Iron_Texture.png", null, null, 1));
							var m1 = ModelUtils.withAttachment(mod1, attachments.toArray(new ModelAttachment[0]));
							var co = new ModelComponent(m1);
							s.putComponent(ref, ModelComponent.getComponentType(), co);


							//s.putComponent(ref, PlayerSkinComponent.getComponentType(), new PlayerSkinComponent(skp.getPlayerSkin()));

						}*/
					}
				});

			}


			//The server side entity still have an invisible hitbox: we can't check for it
			/*EntityRefCollisionProvider collision = new EntityRefCollisionProvider();
			TransformComponent transform = ref.getStore().getComponent(ref, TransformComponent.getComponentType());
			ModelComponent model = ref.getStore().getComponent(ref, ModelComponent.getComponentType());
			HeadRotation headRot = ref.getStore().getComponent(ref, HeadRotation.getComponentType());
			var eyePos = transform.getPosition().clone().add(0, model.getModel().getEyeHeight(), 0);
			var lookDir = com.hypixel.hytale.math.vector.Transform.getDirection(headRot.getRotation().getPitch(), headRot.getRotation().getYaw()).normalize();
			double hitDist = collision.computeNearest(context.getCommandBuffer(), model.getModel().getBoundingBox(), eyePos, lookDir.scale(10.0), ref, null);
			if(collision.getCount() > 0) {
				Ref<EntityStore> hitRef = collision.getContact(0).getEntityReference();
				System.out.println(hitRef);
				if(hitRef != targetedEntity) {
					higlightEntity(ref, hitRef);
				}
				// hitRef = first entity looked at
			}
*/
		}
	}

}
