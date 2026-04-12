package com.diamssword.redCrystal.display;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.worldInteraction.FacingUtil;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.matrix.Matrix4d;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.*;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.player.DisplayDebug;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.PlayerUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RedComponentDisplayUtils {


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
			return ModelUtils.withBB(model, modified);
		}
		return model;
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
		return new TransformComponent(getCenteredPosition(position, face, offset), FacingUtil.facingToRotation(face));
	}

	public static Vector3d getCenteredPosition(Vector3i position, BlockFace face, Vector2d offset) {
		return position.toVector3d().clone().add(0.5, 0.5, 0.5).clone().add(FacingUtil.facingToDir(face, 0.51, offset.x, offset.y));
	}

	public static Vector3d getIOPosition(short index, RedCompBehavior behavior, boolean isOutput) {
		return isOutput ? getOutputPosition(index, behavior) : getInputPosition(index, behavior);
	}

	public static Vector3d getInputPosition(short index, RedCompBehavior behavior) {
		var spacing = behavior.InputsCount() < 6 ? 0.2f : 0.1f;
		return getCenteredPosition(behavior.parent.getParent().getPosition(), behavior.parent.getFace(), new Vector2d((index - (behavior.InputsCount() - 1) / 2f) * spacing, -0.35));
	}

	public static Vector3d getOutputPosition(short index, RedCompBehavior behavior) {

		var spacing = behavior.outputsCount() < 6 ? 0.2f : 0.1f;
		return getCenteredPosition(behavior.parent.getParent().getPosition(), behavior.parent.getFace(), new Vector2d((index - (behavior.outputsCount() - 1) / 2f) * spacing, 0.35));
	}

	public static void createTempRune(EntityStore entityStore, Vector3i position, BlockFace face, RedElement element) {
		var holder = createMinimalDisplayEntity(entityStore, position, face);
		var model = ModelUtils.withTexture(getFlatModel(face), element.getAsset().getTexture());
		//holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
		holder.ensureComponent(Intangible.getComponentType());
		holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent("main", (short) 0, element));
		holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(0.5f));
		holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
		var ref = entityStore.getStore().addEntity(holder, AddReason.SPAWN);
		HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
			if(entityStore.getWorld().isAlive())
				entityStore.getWorld().execute(() -> {entityStore.getStore().removeEntity(ref, RemoveReason.REMOVE);});
		}, 1, TimeUnit.SECONDS);
	}

	public static DisplayEntityGroupHolder createEditEntities(EntityStore entityStore, Vector3i position, BlockFace face, RedElement element) {

		var visibility = element.getSettings().getVisibility();
		if(element.getBehavior() != null) {
			var maxO = element.getBehavior().outputsCount();
			var maxI = element.getBehavior().InputsCount();
			var res = new DisplayEntityGroupHolder(maxI, maxO);
			for(short i = 0; i < maxO; i++) {
				var spacing = maxO < 6 ? 0.2f : 0.1f;
				var holder = createMinimalDisplayEntity(entityStore, position, face, new Vector2d((i - (maxO - 1) / 2f) * spacing, 0.35));
				var disp = new RedEntityHiddenComponent(element, 0.1f, visibility);
				holder.addComponent(RedEntityHiddenComponent.getComponentType(), disp);
				holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(ModelUtils.withTexture(getFlatModel(face), "Items/RedCrystal/Glyphs/Output.png")));
				holder.ensureComponent(Interactable.getComponentType());
				holder.ensureComponent(Intangible.getComponentType());
				holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(disp.getHiddenScale()));
				holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent("output", i, element));
				Interactions interactions = new Interactions();
				//interactions.setInteractionId(InteractionType.Secondary, "*UseRedCrystalEntity");
				//interactions
				interactions.setInteractionHint("RedCrystal.interactionHints.output");
				holder.addComponent(Interactions.getComponentType(), interactions);
				res.setOutput(i, holder);
			}
			for(short i = 0; i < maxI; i++) {
				var spacing = maxI < 6 ? 0.2f : 0.1f;
				var holder = createMinimalDisplayEntity(entityStore, position, face, new Vector2d((i - (maxI - 1) / 2f) * spacing, -0.35));
				holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(ModelUtils.withTexture(getFlatModel(face), "Items/RedCrystal/Glyphs/Input.png")));
				holder.ensureComponent(Interactable.getComponentType());
				holder.ensureComponent(Intangible.getComponentType());
				holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent("input", i, element));
				var disp = new RedEntityHiddenComponent(element, 0.1f, visibility);
				holder.addComponent(RedEntityHiddenComponent.getComponentType(), disp);
				holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(disp.getHiddenScale()));
				Interactions interactions = new Interactions();
				interactions.setInteractionHint("RedCrystal.interactionHints.input");
				holder.addComponent(Interactions.getComponentType(), interactions);
				res.setInput(i, holder);
			}
			var holder = createMinimalDisplayEntity(entityStore, position, face);
			var model = ModelUtils.withTexture(getFlatModel(face), element.getAsset().getTexture());

			holder.ensureComponent(Intangible.getComponentType());
			var disp = new RedEntityHiddenComponent(element, 0.5f, visibility);
			holder.addComponent(RedEntityHiddenComponent.getComponentType(), disp);
			holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent("main", (short) 0, element));
			holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(disp.getHiddenScale()));
			holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
			res.setMain(holder);
			return res;
		}
		return null;
	}


	public static void drawLaser(Store<EntityStore> store, Vector3d from, Vector3d to, float time, short power) {
		PlayerUtil.broadcastPacketToPlayers(store, getBeamPacket(from, to, time, redFromShort(power), 0.03f, false));
	}

	public static Color redColorFromShort(short value) {
		int v = Math.max(0, Math.min(RedCompBehavior.MAX, value & 0xFFFF));

		double t = v / (double) RedCompBehavior.MAX;
		double gamma = 1.8;

		double corrected = Math.pow(t, gamma);
		int baseGray = 60;

		int r = (int) Math.round(baseGray + (255 - baseGray) * corrected);
		int g = (int) Math.round(baseGray * (1 - corrected));
		int b = (int) Math.round(baseGray * (1 - corrected));

		return new Color((byte) r, (byte) g, (byte) b);
	}

	public static int redFromShort(short value) {
		value = (short) Math.min(value, 255);
		int v = Math.max(0, Math.min(RedCompBehavior.MAX, value & 0xFFFF));
		double gamma = 1;                                 // adjust for perceptual brightness
		int r = (int) Math.round(RedCompBehavior.MAX * Math.pow(v / (double) RedCompBehavior.MAX, gamma));
		return 0xFF000000 | (r << 16);
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
		return new DisplayDebug(DebugShape.Cylinder, matrix.asFloatData(), new com.hypixel.hytale.protocol.Vector3f(color.x, color.y, color.z), time, buildFlags(fade), null, 0.8f);

	}

	static byte buildFlags(boolean fade) {
		int flags = 0;
		if(fade)
			flags |= DebugUtils.FLAG_FADE;
		flags |= DebugUtils.FLAG_NO_WIREFRAME;
		//flags |= DebugUtils.FLAG_NO_SOLID;

		return (byte) flags;
	}

	public static void drawLaserFor(Vector3d from, Vector3d to, float time, int color, Ref<EntityStore> player) {
		player.getStore().getComponent(player, PlayerRef.getComponentType()).getPacketHandler().write(getBeamPacket(from, to, time, color, 0.03f, true));
	}

	public static void drawLaserFor(Store<EntityStore> store, Vector3d from, Vector3d to, float time, int color, float size) {
		PlayerUtil.broadcastPacketToPlayers(store, getBeamPacket(from, to, time, color, size, false));
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
