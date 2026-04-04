package com.diamssword.redCrystal.behavior.outputs;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.behavior.base.RedCompBehaviorWithSettings;
import com.diamssword.redCrystal.behavior.modifiers.DelayBehavior;
import com.diamssword.redCrystal.display.ModelUtils;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.diamssword.redCrystal.gui.GlyphSettingsValidators;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
import com.diamssword.redCrystal.worldInteraction.FacingUtil;
import com.diamssword.redCrystal.worldInteraction.FakeCommandSender;
import com.diamssword.redCrystal.worldInteraction.FakeLivingEntity;
import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.commands.CopyCommand;
import com.hypixel.hytale.builtin.buildertools.snapshot.BlockSelectionSnapshot;
import com.hypixel.hytale.builtin.buildertools.snapshot.ClipboardBoundsSnapshot;
import com.hypixel.hytale.builtin.buildertools.snapshot.EntityTransformSnapshot;
import com.hypixel.hytale.builtin.buildertools.snapshot.SelectionSnapshot;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocksound.config.BlockSoundSet;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.block.BlockEntity;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollision;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.LocalCachedChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.core.util.TargetUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class PistonBehavior extends RedCompBehaviorWithSettings<BehaviorAsset, PistonBehavior.PistonSettings> {

	public static BuilderCodec<PistonBehavior.PistonSettings> CODEC = BuilderCodec.builder(PistonBehavior.PistonSettings.class, PistonBehavior.PistonSettings::new)
			.append(new KeyedCodec<>("PistonBehaviorExtend", BuilderCodec.INTEGER), (a, b) -> a.extendLength = b, a -> a.extendLength)
			.addValidator(new GlyphSettingsValidators.SliderRangeValidator<>(1, 16, 1))
			.add()
			.append(new KeyedCodec<>("PistonBehaviorGrab", BuilderCodec.INTEGER), (a, b) -> a.selectionLength = b, a -> a.selectionLength)
			.addValidator(new GlyphSettingsValidators.SliderRangeValidator<>(1, 16, 1))
			.add().build();

	public PistonBehavior(String id, RedElement parent, BehaviorAsset asset) {
		super(id, parent, asset);
	}

	@Override
	public void onSignalChange(short input, short oldValue, short value) {
		if(value > MIN) {

		}
	}

	@Override
	public void tick() {
		super.tick();
		var state = getInputState(0);
		if(state > MIN) {
			var dist = Math.floor(getSettings().extendLength * getInputState(0) / (float) MAX);
			if(getInternalState("extended") < dist)
				moveBlock(false);
			else if(getInternalState("extended") > dist)
				moveBlock(true);
		} else if(getInternalState("extended") > 0)
			moveBlock(true);
	}

	public BlockSelection getSelection(boolean retracting) {
		BlockSelection selec = new BlockSelection();
		var pos = this.parent.getParent().getPosition();
		var size = getMaxSize();
		if(size > 0 && (retracting || size <= this.getSettings().selectionLength)) {
			if(retracting)
				size = Math.min(size, this.getSettings().selectionLength);
			var a = FacingUtil.facingToDir(FacingUtil.opposite(this.parent.getFace()), 1 + getInternalState("extended"), 0, 0).add(pos);
			var b = FacingUtil.facingToDir(FacingUtil.opposite(this.parent.getFace()), getInternalState("extended") + size, 0, 0).add(pos);
			selec.setSelectionArea(a.toVector3i(), b.toVector3i());
			return selec;
		}
		return null;
	}

	public int getMaxSize() {
		var pos = this.parent.getParent().getPosition();
		var vec = FacingUtil.facingToDir(FacingUtil.opposite(this.parent.getFace()), 1, 0, 0);
		var vec1 = FacingUtil.facingToDir(FacingUtil.opposite(this.parent.getFace()), 1 + getInternalState("extended"), 0, 0).add(pos);
		var size = this.getSettings().selectionLength + 1;
		for(int i = 0; i < this.getSettings().selectionLength + 1; i++) {
			var p1 = vec1.clone().add(vec.clone().scale(i)).toVector3i();
			if(!canMove(p1)) {
				size = -1;
				break;
			}
			if(isAir(p1)) {
				size = i;
				break;
			}
		}
		return size;
	}

	public void moveBlock(boolean retract) {

		var pos = this.parent.getParent().getPosition();
		if(retract) {
			if(getWorld().getBlock(FacingUtil.facingToDir(FacingUtil.opposite(this.parent.getFace()), getInternalState("extended"), 0, 0).add(pos).toVector3i()) == 0) {
				move(true, FacingUtil.facingToDir(FacingUtil.opposite(this.parent.getFace()), retract ? -1 : 1, 0, 0).toVector3i(), false, false, getWorld().getEntityStore().getStore());
				playSound();
			}
			setInternalState("extended", (short) (getInternalState("extended") + (retract ? -1 : 1)));

		} else {
			if(getMaxSize() <= this.getSettings().selectionLength) {
				move(false, FacingUtil.facingToDir(FacingUtil.opposite(this.parent.getFace()), retract ? -1 : 1, 0, 0).toVector3i(), false, false, getWorld().getEntityStore().getStore());
				setInternalState("extended", (short) (getInternalState("extended") + (retract ? -1 : 1)));
				playSound();
			}
		}
		//if(getWorld().getBlock(a) == 0)

		/*var pos = this.parent.getParent().getPosition();
		var dest = FacingUtil.facingToDir(FacingUtil.opposite(this.parent.getFace()), 1, 0, 0).add(pos.clone()).toVector3i();
		var block = this.getWorld().getBlock(pos);
		var chunkOrigin = getWorld().getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
		var chunkDest = getWorld().getChunk(ChunkUtil.indexChunkFromBlock(dest.x, dest.z));
		var assetMap = BlockType.getAssetMap();

		//		chunkOrigin.setb
		chunkDest.setBlock(dest.x, dest.y, dest.z, block);
		chunkOrigin.setBlock(pos.x, pos.y, pos.z, 0);
		//LocalCachedChunkAccessor accessor = LocalCachedChunkAccessor.atWorldCoords(world, xMin + halfWidth, zMin + halfDepth, Math.max(width, depth));

		 */
	}

	public boolean isAir(Vector3i pos) {
		return getWorld().getBlock(pos) == 0;
	}

	public boolean canMove(Vector3i pos) {
		var chunk = getWorld().getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
		var blockID = chunk.getBlock(pos);
		//	if(blockID == 0)
		//		return false;
		if(chunk.getFiller(pos.x, pos.y, pos.z) != 0)
			return false;

		var holder = chunk.getBlockComponentHolder(pos.x, pos.y, pos.z);
		if(holder != null)
			return false;
		//return true;
		var block = BlockType.getAssetMap().getAsset(blockID);
		var bb = BlockBoundingBoxes.getAssetMap().getAsset(block.getHitboxType());
		return !bb.protrudesUnitBox();


	}

	public void playSound() {
		var block = getWorld().getBlockType(parent.getParent().getPosition());
		var set = BlockSoundSet.getAssetMap().getAsset(block.getBlockSoundSetIndex());
		String seatSoundId = set == null ? null : set.getSoundEventIds().getOrDefault(BlockSoundEvent.Build, null);
		if(seatSoundId != null) {
			int soundEventIndex = SoundEvent.getAssetMap().getIndex(seatSoundId);
			SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, parent.getParent().getPosition().toVector3d().add(0.5, 0.5, 0.5), getWorld().getEntityStore().getStore());
		}

	}

	public static class PistonSettings {
		public int extendLength = 1;
		public int selectionLength = 1;

		public PistonSettings() {

		}
	}

	public void move(boolean retracting, @Nonnull Vector3i direction, boolean empty, boolean entities, @Nonnull ComponentAccessor<EntityStore> componentAccessor
	) {
		var selection = getSelection(retracting);
		if(selection == null)
			return;
		Vector3i min = Vector3i.min(selection.getSelectionMin(), selection.getSelectionMax());
		Vector3i max = Vector3i.max(selection.getSelectionMin(), selection.getSelectionMax());
		int xMin = min.getX();
		int xMax = max.getX();
		int yMin = min.getY();
		int yMax = max.getY();
		int zMin = min.getZ();
		int zMax = max.getZ();
		BlockSelection selected = new BlockSelection();
		int width = xMax - xMin;
		int depth = zMax - zMin;
		int halfWidth = width / 2;
		int halfDepth = depth / 2;
		int xPos = xMin + halfWidth;
		int zPos = zMin + halfDepth;
		selected.setPosition(xPos, yMin, zPos);
		BlockSelection cleared = new BlockSelection(selected);
		World world = componentAccessor.getExternalData().getWorld();
		LocalCachedChunkAccessor accessor = LocalCachedChunkAccessor.atWorldCoords(world, xMin + halfWidth, zMin + halfDepth, Math.max(width, depth) + 16);
		BlockTypeAssetMap<String, BlockType> blockTypeAssetMap = BlockType.getAssetMap();
		IndexedLookupTableAssetMap<String, BlockBoundingBoxes> hitboxAssetMap = BlockBoundingBoxes.getAssetMap();

		for(int x = xMin; x <= xMax; x++) {
			for(int z = zMin; z <= zMax; z++) {
				WorldChunk chunk = accessor.getChunk(ChunkUtil.indexChunkFromBlock(x, z));

				for(int y = yMax; y >= yMin; y--) {
					int block = chunk.getBlock(x, y, z);
					//int fluidId = chunk.getFluidId(x, y, z);
					//byte fluidLevel = chunk.getFluidLevel(x, y, z);
					if((block != 0 || empty)) {
						int filler = chunk.getFiller(x, y, z);
						int rotationIndex = chunk.getRotationIndex(x, y, z);
						selected.addBlockAtWorldPos(
								x, y, z, block, rotationIndex, filler, chunk.getSupportValue(x, y, z), chunk.getBlockComponentHolder(x, y, z)
						);
						//	selected.addFluidAtWorldPos(x, y, z, fluidId, fluidLevel);
						cleared.addBlockAtWorldPos(x, y, z, 0, 0, 0, 0);
						//	cleared.addFluidAtWorldPos(x, y, z, 0, (byte) 0);
						if(filler == 0 && block != 0) {
							BlockType blockType = blockTypeAssetMap.getAsset(block);
							if(blockType != null) {
								BlockBoundingBoxes hitbox = hitboxAssetMap.getAsset(blockType.getHitboxTypeIndex());
								if(hitbox != null && hitbox.protrudesUnitBox()) {
									int baseX = x;
									int baseY = y;
									int baseZ = z;
									FillerBlockUtil.forEachFillerBlock(
											hitbox.get(rotationIndex),
											(fx, fy, fz) -> {
												if(fx != 0 || fy != 0 || fz != 0) {
													int fillerX = baseX + fx;
													int fillerY = baseY + fy;
													int fillerZ = baseZ + fz;
													if(fillerX < xMin || fillerX > xMax || fillerY < yMin || fillerY > yMax || fillerZ < zMin || fillerZ > zMax) {
														WorldChunk fillerChunk = accessor.getChunk(ChunkUtil.indexChunkFromBlock(fillerX, fillerZ));
														int fillerBlock = fillerChunk.getBlock(fillerX, fillerY, fillerZ);
														int fillerFiller = fillerChunk.getFiller(fillerX, fillerY, fillerZ);
														if(fillerFiller != 0) {
															int fillerRotation = fillerChunk.getRotationIndex(fillerX, fillerY, fillerZ);
															selected.addBlockAtWorldPos(
																	fillerX,
																	fillerY,
																	fillerZ,
																	fillerBlock,
																	fillerRotation,
																	fillerFiller,
																	fillerChunk.getSupportValue(fillerX, fillerY, fillerZ),
																	fillerChunk.getBlockComponentHolder(fillerX, fillerY, fillerZ)
															);
															cleared.addBlockAtWorldPos(fillerX, fillerY, fillerZ, 0, 0, 0, 0);
														}
													}
												}
											}
									);
								}
							}
						}
					}
				}
			}
		}
		var sender = new FakeCommandSender();
		BlockSelection beforeCleared = cleared.place(sender, world);
		selected.setPosition(xPos + direction.getX(), yMin + direction.getY(), zPos + direction.getZ());
		BlockSelection beforePlace = selected.place(sender, world);
		List<SelectionSnapshot<?>> snapshots = new ObjectArrayList<>();
		if(entities) {
			for(Ref<EntityStore> targetEntityRef : TargetUtil.getAllEntitiesInBox(min.toVector3d(), max.toVector3d(), componentAccessor)) {
				snapshots.add(new EntityTransformSnapshot(targetEntityRef, componentAccessor));
				TransformComponent transformComponent = componentAccessor.getComponent(targetEntityRef, TransformComponent.getComponentType());
				if(transformComponent != null) {
					transformComponent.getPosition().add(direction);
				}
			}
		}

		beforePlace.add(beforeCleared);
		ClipboardBoundsSnapshot clipboardSnapshot = new ClipboardBoundsSnapshot(min, max);
		Vector3i destMin = min.clone().add(direction);
		Vector3i destMax = max.clone().add(direction);
		beforePlace.setSelectionArea(Vector3i.min(min, destMin), Vector3i.max(max, destMax));
		snapshots.add(new BlockSelectionSnapshot(beforePlace));
		snapshots.add(clipboardSnapshot);
		//this.pushHistory(BuilderToolsPlugin.Action.MOVE, snapshots);
		BuilderToolsPlugin.invalidateWorldMapForSelection(cleared, world);
		BuilderToolsPlugin.invalidateWorldMapForSelection(selected, world);
		selection.setSelectionArea(min.add(direction), max.add(direction));

		//this.sendUpdate();
		//this.sendArea();
	}
}
