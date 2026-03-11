package com.diamssword.redCrystal.interaction;


import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.diamssword.redCrystal.redComponent.RedCompBehavior;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class WandEntityInteraction extends SimpleInteraction {
	@Nonnull
	public static final BuilderCodec<WandEntityInteraction> CODEC = BuilderCodec.builder(WandEntityInteraction.class, WandEntityInteraction::new, SimpleInteraction.CODEC)
			.appendInherited(
					new KeyedCodec<>("RemoveMode", Codec.BOOLEAN),
					(interaction, s) -> interaction.removeMode = s,
					interaction -> interaction.removeMode,
					(interaction, parent) -> interaction.removeMode = parent.removeMode
			)
			.documentation("Determines whether to remove the interacted component or add one")
			.add()
			.build();

	private boolean removeMode = false;

	@Override
	protected void tick0(boolean firstRun, float time, @NotNull InteractionType type, @NotNull InteractionContext context, @NotNull CooldownHandler cooldownHandler) {
		super.tick0(firstRun, time, type, context, cooldownHandler);
		if(firstRun && context.getTargetEntity() != null) {
			var entity = context.getTargetEntity();
			var compLink = entity.getStore().getComponent(entity, RedEntityLinkComponent.getComponentType());
			if(compLink != null) {
				interactWithEntity(context.getCommandBuffer().getExternalData().getWorld(), context, context.getHeldItem(), entity, compLink);
			} else
				context.getState().state = InteractionState.Failed;
		} else
			context.getState().state = InteractionState.Failed;
	}

	protected void interactWithEntity(@NonNullDecl World world, @NonNullDecl InteractionContext context, @NullableDecl ItemStack stack, @NonNullDecl Ref<EntityStore> target, @NonNullDecl RedEntityLinkComponent link) {
		var client = context.getClientState();
		assert client != null;
		if(link.getLinked().isValid()) {
			link.getLinked().getBehavior().onEntityInteract(link.getPart(), link.getPartIndex(), context.getOwningEntity(), target, context, removeMode ? RedCompBehavior.InteractType.Remove : RedCompBehavior.InteractType.Use);

		} else
			context.getState().state = InteractionState.Failed;


	}
}
