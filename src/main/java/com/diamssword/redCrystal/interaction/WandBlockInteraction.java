package com.diamssword.redCrystal.interaction;


import com.diamssword.redCrystal.storage.RedElementState;
import com.diamssword.redCrystal.storage.RedWandStorage;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
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
			.documentation("Determines whether to removed the interacted component or add one")
			.add()
			.build();

	private boolean removeMode = false;

	@Override
	protected void interactWithBlock(@NonNullDecl World world, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext context, @NullableDecl ItemStack stack, @NonNullDecl Vector3i targetBlock, @NonNullDecl CooldownHandler cooldownHandler) {
		var client = context.getClientState();
		assert client != null;
		if(removeMode) {
			var comp = getBlockState(world, targetBlock.x, targetBlock.y, targetBlock.z);
			if(comp != null) {
				var removed = comp.removeElement(client.blockFace);

				if(comp.getAllElements().isEmpty()) {
					removeBlockState(world, targetBlock.x, targetBlock.y, targetBlock.z);
				}
			} else
				context.getState().state = InteractionState.Failed;
		} else {
			var state = getOrCreateBlockState(world, targetBlock.x, targetBlock.y, targetBlock.z);
			if(state != null && state.getElement(client.blockFace) == null) {
				var toolSettings = stack.getFromMetadataOrDefault("RedCrystalToolSettings", RedWandStorage.CODEC);
				var glyph = toolSettings.getSelectedGlyph();
				if(stack != null) {
					var tags = stack.getItem().getData().getRawTags();
					var gl = tags.get("RedCrystalGlyph");
					if(gl != null && gl.length > 0)
						glyph = gl[gl.length - 1];
				}
				if(glyph != null)
					context.getState().state = state.getOrCreateElement(client.blockFace, glyph) != null ? InteractionState.Finished : InteractionState.Failed;
				else
					context.getState().state = InteractionState.Failed;
			} else
				context.getState().state = InteractionState.Failed;
		}

	}

	private void removeBlockState(World world, int x, int y, int z) {
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

	private RedElementState getBlockState(World world, int x, int y, int z) {
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
				redState.setPosition(new Vector3i(x, y, z), chunkRef);
				return redState;
			} else {
				var red = world.getChunkStore().getStore().ensureAndGetComponent(blockRef, RedElementState.getComponent());
				red.setPosition(new Vector3i(x, y, z), chunkRef);
				return red;
			}
		}
	}

	@Override
	protected void simulateInteractWithBlock(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock) {

	}
}
