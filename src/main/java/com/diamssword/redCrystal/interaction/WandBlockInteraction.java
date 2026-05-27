package com.diamssword.redCrystal.interaction;


import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.storage.PlayerDatas;
import com.diamssword.redCrystal.storage.RedElementState;
import com.diamssword.redCrystal.wand.RedWandTool;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3i;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WandBlockInteraction extends SimpleBlockInteraction {
	@Nonnull
	public static final BuilderCodec<WandBlockInteraction> CODEC = BuilderCodec.builder(WandBlockInteraction.class, WandBlockInteraction::new, SimpleBlockInteraction.CODEC)
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
	protected void interactWithBlock(@NonNullDecl World world, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext context, @NullableDecl ItemStack stack, @NonNullDecl Vector3i targetBlock, @NonNullDecl CooldownHandler cooldownHandler) {
		var client = context.getClientState();
		assert client != null;
		if(removeMode) {
			var comp = getBlockState(world, targetBlock.x, targetBlock.y, targetBlock.z);
			tryRemoveRune(world, comp, client.blockFace, context);
			if(context.getState().state == InteractionState.Finished) {
				var targetBlockProtocol = new com.hypixel.hytale.protocol.Vector3i(targetBlock.x, targetBlock.y, targetBlock.z);
				RedWandTool.playSound("Break", targetBlockProtocol, context.getEntity(), commandBuffer);
				RedWandTool.playParticle(targetBlockProtocol, client.blockFace, commandBuffer);

			}
		} else if(stack != null) {
			if((stack.getMaxDurability() == 0 || stack.getDurability() > 0)) {
				var state = getOrCreateBlockState(world, targetBlock.x, targetBlock.y, targetBlock.z);
				if(state != null) {
					var player = context.getEntity().getStore().getComponent(context.getEntity(), PlayerRef.getComponentType());
					if(RedWandTool.createRune(stack, state, client.blockFace, context.getEntity())) {
						if(context.getHeldItemContainer() != null) {
							var slot = context.getHeldItemSlot();
							context.getHeldItemContainer().replaceItemStackInSlot(slot, stack, stack.withIncreasedDurability(-1));
						}

						context.getState().state = InteractionState.Finished;
						var targetBlockProtocol = new com.hypixel.hytale.protocol.Vector3i(targetBlock.x, targetBlock.y, targetBlock.z);
						RedWandTool.playParticle(targetBlockProtocol, client.blockFace, commandBuffer);
						RedWandTool.playSound("Place", targetBlockProtocol, player.getReference(), commandBuffer);
					} else {
						var element = state.getElement(client.blockFace);
						if(element != null) {
							if(player != null) {
								element.getBehavior().onMainRuneInteract(context.getEntity(), null, context, RedCompBehavior.InteractType.Use);
								context.getState().state = InteractionState.Finished;
							} else
								context.getState().state = InteractionState.Failed;
						} else
							context.getState().state = InteractionState.Failed;
					}
				} else
					context.getState().state = InteractionState.Failed;
			} else
				context.getState().state = InteractionState.Failed;
		} else
			context.getState().state = InteractionState.Failed;

	}

	public static void tryRemoveRune(World world, RedElementState comp, BlockFace face, InteractionContext context) {
		if(comp != null) {
			var removed = comp.removeElement(face);
			if(comp.getAllElements().isEmpty()) {
				var targetBlock = comp.getPosition();
				removeBlockState(world, targetBlock.x, targetBlock.y, targetBlock.z);
			}
			var pcomp = context.getCommandBuffer().getComponent(context.getEntity(), PlayerDatas.getComponentType());
			if(pcomp != null)
				pcomp.invalidateHovered();
			if(removed != null && context.getHeldItem() != null && context.getHeldItemContainer() != null) {
				var slot = context.getHeldItemSlot();
				if(context.getHeldItem().getDurability() < context.getHeldItem().getMaxDurability())
					context.getHeldItemContainer().replaceItemStackInSlot(slot, context.getHeldItem(), context.getHeldItem().withIncreasedDurability(1));
				else {
					world.execute(() -> world.getEntityStore().getStore().addEntity(RedWandTool.dropDust(world.getEntityStore().getStore(), 1, comp.getPosition(), face), AddReason.SPAWN));
				}
			}
			context.getState().state = removed != null ? InteractionState.Finished : InteractionState.Failed;
		} else
			context.getState().state = InteractionState.Failed;
	}

	private static void removeBlockState(World world, int x, int y, int z) {
		Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(ChunkUtil.indexChunkFromBlock(x, z));
		if(chunkRef != null) {

			BlockComponentChunk blockComponentChunk = world.getChunkStore().getStore().getComponent(chunkRef, BlockComponentChunk.getComponentType());
			if(blockComponentChunk != null) {
				int blockIndexColumn = ChunkUtil.indexBlockInColumn(x, y, z);
				Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndexColumn);
				if(blockRef != null) {
					world.execute(() -> {
						if(blockRef.getStore().getArchetype(blockRef).asExactQuery().test(Archetype.of(RedElementState.getComponent(), BlockModule.BlockStateInfo.getComponentType()))) {
							blockRef.getStore().removeEntity(blockRef, RemoveReason.REMOVE);
						} else {
							blockRef.getStore().removeComponentIfExists(blockRef, RedElementState.getComponent());

						}
					});

				}
			}
		}
	}

	public static RedElementState getBlockState(World world, int x, int y, int z) {
		Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(ChunkUtil.indexChunkFromBlock(x, z));
		if(chunkRef == null)
			return null;
		BlockComponentChunk blockComponentChunk = world.getChunkStore().getStore().getComponent(chunkRef, BlockComponentChunk.getComponentType());
		if(blockComponentChunk == null) {
			return null;
		} else {
			int blockIndexColumn = ChunkUtil.indexBlockInColumn(x, y, z);
			Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndexColumn);
			if(blockRef == null) {
				return null;
			} else {
				return world.getChunkStore().getStore().getComponent(blockRef, RedElementState.getComponent());
			}
		}
	}


	private RedElementState getOrCreateBlockState(World world, int x, int y, int z) {
		Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(ChunkUtil.indexChunkFromBlock(x, z));
		if(chunkRef == null)
			return null;
		BlockComponentChunk blockComponentChunk = world.getChunkStore().getStore().getComponent(chunkRef, BlockComponentChunk.getComponentType());
		if(blockComponentChunk == null) {
			return null;
		} else {
			int blockIndexColumn = ChunkUtil.indexBlockInColumn(x, y, z);
			Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndexColumn);
			RedElementState redState;
			if(blockRef == null) {
				Holder<ChunkStore> blockEntity = ChunkStore.REGISTRY.newHolder();
				blockEntity.putComponent(BlockModule.BlockStateInfo.getComponentType(), new BlockModule.BlockStateInfo(blockIndexColumn, chunkRef));
				redState = new RedElementState();
				blockEntity.addComponent(RedElementState.getComponent(), redState);
				world.getChunkStore().getStore().addEntity(blockEntity, AddReason.SPAWN);
				redState.setPosition(new com.hypixel.hytale.protocol.Vector3i(x, y, z), chunkRef);
				return redState;
			} else {
				var red = world.getChunkStore().getStore().getComponent(blockRef, RedElementState.getComponent());
				if(red == null) {
					red = world.getChunkStore().getStore().ensureAndGetComponent(blockRef, RedElementState.getComponent());
					red.setPosition(new com.hypixel.hytale.protocol.Vector3i(x, y, z), chunkRef);
				}
				return red;
			}
		}
	}

	@Override
	protected void simulateInteractWithBlock(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock) {

	}
}
