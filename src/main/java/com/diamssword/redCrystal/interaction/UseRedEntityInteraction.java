package com.diamssword.redCrystal.interaction;

import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.logging.Level;
import javax.annotation.Nonnull;

public class UseRedEntityInteraction extends SimpleInstantInteraction {
	public static final BuilderCodec<UseRedEntityInteraction> CODEC = BuilderCodec.builder(
					UseRedEntityInteraction.class, UseRedEntityInteraction::new, SimpleInstantInteraction.CODEC
			)
			.documentation("Interacts with a target NPC.")
			.build();
	public static final String DEFAULT_ID = "*UseRedCrystalEntity";
	public static final RootInteraction DEFAULT_ROOT = new RootInteraction("*UseRedCrystalEntity", "*UseRedCrystalEntity");

	public UseRedEntityInteraction(String id) {
		super(id);
	}

	protected UseRedEntityInteraction() {
	}

	@Override
	protected final void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
		Ref<EntityStore> ref = context.getEntity();
		CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
		Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
		if(playerComponent == null) {
			HytaleLogger.getLogger().at(Level.INFO).log("UseRedEntityInteraction requires a Player but was used for: %s", ref);
			context.getState().state = InteractionState.Failed;
		} else {
			Ref<EntityStore> targetRef = context.getTargetEntity();
			if(targetRef == null) {
				context.getState().state = InteractionState.Failed;
			} else {
				RedEntityLinkComponent redLinkComp = commandBuffer.getComponent(targetRef, RedEntityLinkComponent.getComponentType());
				if(redLinkComp == null) {
					HytaleLogger.getLogger().at(Level.INFO).log("UseRedEntityInteraction requires a target RedEntityLinkComponent but was used for: %s", targetRef);
					context.getState().state = InteractionState.Failed;
				} else if(redLinkComp.getLinked() == null) {
					context.getState().state = InteractionState.Failed;
				} else {
					redLinkComp.getLinked().getBehavior().onEntityInteract(redLinkComp.getPart(), redLinkComp.getPartIndex(), ref, targetRef);
					context.getState().state = InteractionState.Finished;

				}
			}
		}
	}

	@Nonnull
	@Override
	public String toString() {
		return "UseRedEntityInteraction{} " + super.toString();
	}
}
