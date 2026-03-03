package com.diamssword.redCrystal.interaction;

import com.diamssword.redCrystal.display.RedEntityHiddenComponent;
import com.diamssword.redCrystal.network.NetworkUtil;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.entities.EntityUpdates;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.receiver.IPacketReceiver;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WandRevealInteraction extends SimpleInteraction {
	public static final BuilderCodec<WandRevealInteraction> CODEC = BuilderCodec.builder(WandRevealInteraction.class, WandRevealInteraction::new, SimpleInteraction.CODEC)

			.build();


	Set<Ref<EntityStore>> processedEntities = new HashSet<>();
	float last = 0;

	void sendUpdateForNewEntities(Ref<EntityStore> playerRef) {

		var visibleEntities = NetworkUtil.getVisibleEntities(playerRef).stream().filter(e -> !processedEntities.contains(e)).collect(Collectors.toSet());

		for(Ref<EntityStore> entityRef : visibleEntities) {
			var hidd = entityRef.getStore().getComponent(entityRef, RedEntityHiddenComponent.getComponentType());
			if(hidd != null) {
				hidd.addSeeingPlayer(playerRef);
				this.processedEntities.add(entityRef);
				try {
					ModelComponent modelComponent = entityRef.getStore().getComponent(entityRef, ModelComponent.getComponentType());
					if(modelComponent == null)
						continue;
					Model model = new Model(modelComponent.getModel());
					ModelUpdate update = new ModelUpdate();
					update.model = model.toPacket();
					update.entityScale = hidd.baseScale;
					NetworkUtil.sendEntityComponentUpdateToPlayer(playerRef, entityRef, new ComponentUpdateType[]{ComponentUpdateType.Intangible}, new ComponentUpdate[]{update});
				} catch(Exception exception) {}
			}
		}
	}

	void sendUpdateToAllPreviousEntities(Ref<EntityStore> playerRef) {
		for(Iterator<Ref<EntityStore>> it = this.processedEntities.iterator(); it.hasNext(); ) {
			Ref<EntityStore> entityRef = it.next();
			it.remove();
			try {
				var hidd = entityRef.getStore().getComponent(entityRef, RedEntityHiddenComponent.getComponentType());
				if(hidd != null)
					hidd.removeSeeingPlayer(playerRef);
				ModelComponent modelComponent = entityRef.getStore().getComponent(entityRef, ModelComponent.getComponentType());
				if(modelComponent == null)
					continue;
				Model model = new Model(modelComponent.getModel());
				ModelUpdate update = new ModelUpdate();
				update.model = model.toPacket();
				update.entityScale = 0.000001f;
				NetworkUtil.sendEntityComponentUpdateToPlayer(playerRef, entityRef, null, new ComponentUpdate[]{update, new IntangibleUpdate()});
			} catch(Exception exception) {}
		}
	}

	@Override
	public void handle(@NonNullDecl Ref<EntityStore> ref, boolean firstRun, float time, @NonNullDecl InteractionType type, @NonNullDecl InteractionContext context) {
		super.handle(ref, firstRun, time, type, context);
		if(context.getState().state != InteractionState.NotFinished) {
			sendUpdateToAllPreviousEntities(ref);
		}
	}

	protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
		super.tick0(firstRun, time, type, context, cooldownHandler);
		if(((firstRun || time > last + 0.5) && (context.getState()).state == InteractionState.NotFinished)) {
			last = time;
			Ref<EntityStore> ref = context.getEntity();
			sendUpdateForNewEntities(ref);

		}
	}

}
