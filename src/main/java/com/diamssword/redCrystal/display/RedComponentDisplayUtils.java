package com.diamssword.redCrystal.display;

import com.diamssword.redCrystal.redComponent.RedCompBehavior;
import com.diamssword.redCrystal.storage.RedElement;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.matrix.Matrix4d;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.*;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolLaserPointer;
import com.hypixel.hytale.protocol.packets.player.DisplayDebug;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.PlayerUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class RedComponentDisplayUtils {
	public static Vector3f rotationToVec(BlockFace face) {
		return toRadians(switch(face) {
			case None, North -> new Vector3f(0, 0, 0);
			case Up -> new Vector3f(90, 0, 0);
			case Down -> new Vector3f(-90, 0, 0);
			case West -> new Vector3f(0, 90, 0);
			case East -> new Vector3f(0, -90, 0);
			case South -> new Vector3f(0, 180, 0);
		});
	}

	public static Model getFlatModel(BlockFace facing) {
		return modifyBoundingBox(Model.createScaledModel(ModelAsset.getAssetMap().getAsset("RedCrystal_Glyph_Flat"), 1f), facing);
	}

	public static Model modifyBoundingBox(Model model, BlockFace facing) {
		var curr = model.getBoundingBox();
		if(curr != null) {
			var modified = (switch(facing) {
				case Up, Down -> new Box(curr.min.x, curr.min.z, curr.min.y, curr.max.x, curr.max.z, curr.max.y);
				case None, North, South -> curr.clone();
				case East, West -> new Box(curr.min.z, curr.min.y, curr.min.x, curr.max.z, curr.max.y, curr.max.x);
			});
			return withBB(model, modified);
		}
		return model;
	}

	/**
	 * Turn a degreeVector to a Radians Vector
	 */
	private static Vector3f toRadians(Vector3f degreeVec) {
		return new Vector3f((float) Math.toRadians(degreeVec.x), (float) Math.toRadians(degreeVec.y), (float) Math.toRadians(degreeVec.z));
	}

	public static Vector3d rotationoffset(BlockFace face, double scale, double addedX, double addedY) {
		return switch(face) {
			case None -> new Vector3d(0, 0, 0);
			case Up -> new Vector3d(addedX, scale, addedY);
			case Down -> new Vector3d(addedX, -scale, addedY);
			case East -> new Vector3d(scale, addedY, addedX);
			case West -> new Vector3d(-scale, addedY, -addedX);
			case North -> new Vector3d(-addedX, addedY, -scale);
			case South -> new Vector3d(addedX, addedY, scale);
		};
	}

	public static Holder<EntityStore> createMinimalDisplayEntity(EntityStore entityStore, Vector3i position, BlockFace face) {
		return createMinimalDisplayEntity(entityStore, position, face, new Vector2d(0, 0));
	}

	public static Holder<EntityStore> createMinimalDisplayEntity(EntityStore entityStore, Vector3i position, BlockFace face, Vector2d offset) {
		Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
		holder.addComponent(TransformComponent.getComponentType(), getCenteredTransform(position, face, offset));
		holder.ensureComponent(UUIDComponent.getComponentType());
		holder.addComponent(NetworkId.getComponentType(), new NetworkId(entityStore.takeNextNetworkId()));
		return holder;
	}

	public static TransformComponent getCenteredTransform(Vector3i position, BlockFace face, Vector2d offset) {
		return new TransformComponent(getCenteredPosition(position, face, offset), rotationToVec(face));
	}

	public static Vector3d getCenteredPosition(Vector3i position, BlockFace face, Vector2d offset) {
		return position.toVector3d().clone().add(0.5, 0.5, 0.5).clone().add(rotationoffset(face, 0.51, offset.x, offset.y));
	}

	public static Vector3d getIOPosition(short index, RedCompBehavior behavior, boolean isOutput) {
		return isOutput ? getOutputPosition(index, behavior) : getInputPosition(index, behavior);
	}

	public static Vector3d getInputPosition(short index, RedCompBehavior behavior) {
		return getCenteredPosition(behavior.parent.getParent().getPosition(), behavior.parent.getFace(), new Vector2d(index / (double) behavior.maxInputs(), -0.35));
	}

	public static Vector3d getOutputPosition(short index, RedCompBehavior behavior) {
		return getCenteredPosition(behavior.parent.getParent().getPosition(), behavior.parent.getFace(), new Vector2d(index / (double) behavior.maxOutputs(), 0.35));
	}

	public static DisplayEntityGroupHolder createEditEntities(EntityStore entityStore, Vector3i position, BlockFace face, RedElement element) {

		if(element.getBehavior() != null) {
			var maxO = element.getBehavior().maxOutputs();
			var maxI = element.getBehavior().maxInputs();
			var res = new DisplayEntityGroupHolder(maxI, maxO);
			for(short i = 0; i < maxO; i++) {
				var holder = createMinimalDisplayEntity(entityStore, position, face, new Vector2d(i / (double) maxO, 0.35));
				holder.addComponent(RedEntityHiddenComponent.getComponentType(), new RedEntityHiddenComponent(element, 0.1f));
				holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(withTexture(getFlatModel(face), "Items/RedCrystal/Glyphs/Output.png")));
				holder.ensureComponent(Interactable.getComponentType());
				holder.ensureComponent(Intangible.getComponentType());
				holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(0.00001f));
				holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent("output", i, element));
				Interactions interactions = new Interactions();
				interactions.setInteractionId(InteractionType.Use, "*UseRedCrystalEntity");  // e.g., "*UseNPC" or custom RootInteraction asset ID
				//interactions.setInteractionHint("your.hint.key");  // Optional client hint text
				holder.addComponent(Interactions.getComponentType(), interactions);
				res.setOutput(i, holder);
			}
			for(short i = 0; i < maxI; i++) {
				var holder = createMinimalDisplayEntity(entityStore, position, face, new Vector2d(i / (double) maxI, -0.35));
				holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(withTexture(getFlatModel(face), "Items/RedCrystal/Glyphs/Input.png")));
				holder.ensureComponent(Interactable.getComponentType());
				holder.ensureComponent(Intangible.getComponentType());
				holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent("input", i, element));
				holder.addComponent(RedEntityHiddenComponent.getComponentType(), new RedEntityHiddenComponent(element, 0.1f));
				holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(0.00001f));
				Interactions interactions = new Interactions();
				interactions.setInteractionId(InteractionType.Use, "*UseRedCrystalEntity");  // e.g., "*UseNPC" or custom RootInteraction asset ID
				//interactions.setInteractionHint("your.hint.key");  // Optional client hint text
				holder.addComponent(Interactions.getComponentType(), interactions);
				res.setInput(i, holder);
			}
			var holder = createMinimalDisplayEntity(entityStore, position, face);
			var model = withTexture(getFlatModel(face), "Items/RedCrystal/Glyphs/" + element.getBehavior().id + ".png");

			//holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
			holder.ensureComponent(Intangible.getComponentType());
			holder.addComponent(RedEntityHiddenComponent.getComponentType(), new RedEntityHiddenComponent(element, 0.5f));
			holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent("main", (short) 0, element));
			holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(0.00001f));
			holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
			res.setMain(holder);
			return res;
		}
		return null;
	}

	public static Model withBB(Model base, Box box) {
		return new Model(
				base.getModelAssetId(),
				base.getScale(),
				base.getRandomAttachmentIds(),
				base.getAttachments(),
				box,
				base.getModel(),
				base.getTexture(),
				base.getGradientSet(),
				base.getGradientId(),
				base.getEyeHeight(),
				base.getCrouchOffset(),
				base.getSittingOffset(),
				base.getSleepingOffset(),
				base.getAnimationSetMap(),
				base.getCamera(),
				base.getLight(),
				base.getParticles(),
				base.getTrails(),
				base.getPhysicsValues(),
				base.getDetailBoxes(),
				base.getPhobia(),
				base.getPhobiaModelAssetId()
		);
	}

	public static Model withTexture(Model base, String texture) {
		return new Model(
				base.getModelAssetId(),
				base.getScale(),
				base.getRandomAttachmentIds(),
				base.getAttachments(),
				base.getBoundingBox(),
				base.getModel(),
				texture,
				base.getGradientSet(),
				base.getGradientId(),
				base.getEyeHeight(),
				base.getCrouchOffset(),
				base.getSittingOffset(),
				base.getSleepingOffset(),
				base.getAnimationSetMap(),
				base.getCamera(),
				base.getLight(),
				base.getParticles(),
				base.getTrails(),
				base.getPhysicsValues(),
				base.getDetailBoxes(),
				base.getPhobia(),
				base.getPhobiaModelAssetId()
		);
	}

	public static Model withModel(Model base, String newModel, String texture) {
		return new Model(
				base.getModelAssetId(),
				base.getScale(),
				base.getRandomAttachmentIds(),
				base.getAttachments(),
				base.getBoundingBox(),
				newModel,
				texture,
				base.getGradientSet(),
				base.getGradientId(),
				base.getEyeHeight(),
				base.getCrouchOffset(),
				base.getSittingOffset(),
				base.getSleepingOffset(),
				base.getAnimationSetMap(),
				base.getCamera(),
				base.getLight(),
				base.getParticles(),
				base.getTrails(),
				base.getPhysicsValues(),
				base.getDetailBoxes(),
				base.getPhobia(),
				base.getPhobiaModelAssetId()
		);
	}

	public static void drawLaser(Store<EntityStore> store, Vector3d from, Vector3d to, float time, short power) {
		PlayerUtil.broadcastPacketToPlayers(store, getBeamPacket(from, to, time, redFromShort(power), 0.1f, false));
	}

	public static int redFromShort(short value) {
		int v = Math.max(0, Math.min(255, value & 0xFFFF)); // clamp 0–255
		double gamma = 2.2;                                 // adjust for perceptual brightness
		int r = (int) Math.round(255 * Math.pow(v / 255.0, gamma));
		return 0xFF000000 | (r << 16); // ARGB: alpha=255, red=r, green=blue=0
	}

	public static DisplayDebug getBeamPacket(Vector3d from, Vector3d to, float time, int color, float scale, boolean fade) {
		float r = ((color >> 16) & 0xFF) / 255.0f;
		float g = ((color >> 8) & 0xFF) / 255.0f;
		float b = (color & 0xFF) / 255.0f;
		Vector3f col = new Vector3f(r, g, b);
		Vector3d relativeTargetOffset = new Vector3d(to.x - from.x, to.y - from.y, to.z - from.z);

		return getBeamPacket(from, relativeTargetOffset, col, time, scale, fade);
	}

	public static DisplayDebug getBeamPacket(@Nonnull Vector3d position, @Nonnull Vector3d direction, @Nonnull Vector3f color, float time, float scale, boolean fade) {
		Vector3d directionClone = direction.clone();
		Matrix4d tmp = new Matrix4d();
		Matrix4d matrix = new Matrix4d();
		matrix.identity();
		matrix.translate(position);
		double angleY = Math.atan2(directionClone.z, directionClone.x);
		matrix.rotateAxis(angleY + (Math.PI / 2), 0.0, 1.0, 0.0, tmp);
		double angleX = Math.atan2(Math.sqrt(directionClone.x * directionClone.x + directionClone.z * directionClone.z), directionClone.y);
		matrix.rotateAxis(angleX, 1.0, 0.0, 0.0, tmp);
		matrix.translate(0.0, directionClone.length() * 0.5, 0.0);
		matrix.scale(scale, directionClone.length(), scale);
		return new DisplayDebug(DebugShape.Cylinder, matrix.asFloatData(), new com.hypixel.hytale.protocol.Vector3f(color.x, color.y, color.z), time, fade, null, 0.8f);

	}

	public static void drawLaserFor(Vector3d from, Vector3d to, float time, int color, Ref<EntityStore> player) {
		player.getStore().getComponent(player, PlayerRef.getComponentType()).getPacketHandler().write(getBeamPacket(from, to, time, color, 0.05f, true));
	}

	public static void drawBeam(Store<EntityStore> store, Vector3d from, Vector3d to, Ref<EntityStore> player) {

		Vector3d relative = new Vector3d().assign(to).subtract(from);  // to - from
		//Vector3f rotation = Vector3f.lookAt(relative);  // Or pass result Vector3f
		//SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = store.getResource(EntityModule.get().getPlayerSpatialResourceType());
		//ObjectList<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
		//playerSpatialResource.getSpatialStructure().collect(from, 75.0, playerRefs);
		Vector3d relativeTargetOffset = new Vector3d(from.x - to.x, from.y - to.y, from.z - to.z);
		var rotation = Vector3f.lookAt(relativeTargetOffset.negate());

		ParticleUtil.spawnParticleEffect("Test", from.x, from.y, from.z, (float) (Math.toRadians(90) + rotation.y), rotation.z, rotation.x, 0.1f, null, null, List.of(player), store);
	}
}
