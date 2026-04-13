package com.diamssword.redCrystal.worldInteraction;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.collision.CollisionMath;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollision;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;

public class CollideUtil {

	public static double blockDistance(World world, Vector3d pos, BlockFace dir, float length) {
		//var length1 = FacingUtil.isNegative(dir) ? -length : length;
		var ray = FacingUtil.facingToDir(dir, length, 0, 0);
		for(var i = 0; i < length; i++) {
			var bpos = new Vector3i().set(new Vector3d(pos).add(FacingUtil.facingToDir(dir, i, 0, 0)));
			var bid = world.getBlock(bpos);
			if(bid != 0) {
				BlockType type = BlockType.getAssetMap().getAsset(bid);
				var box = BlockBoundingBoxes.getAssetMap().getAsset(type.getHitboxType());
				var rotated = box.get(world.getBlockRotationIndex(bpos.x, bpos.y, bpos.z)).getBoundingBox();
				var inter = CollisionMath.intersectRayAABB(pos, ray, bpos.x, bpos.y, bpos.z, rotated);
				if(inter != -Double.MAX_VALUE) {
					var neg = FacingUtil.isNegative(dir);
					if(neg)
						return i + (1 - FacingUtil.extractAxis(dir, rotated.max));
					else
						return i + FacingUtil.extractAxis(dir, rotated.min);
				}

			}
		}
		return -1;
	}

	public static boolean isEntityInBox(Ref<EntityStore> entity, Box box) {
		return !filterEntitiesInBox(entity.getStore(), List.of(entity), box).isEmpty();
	}

	public static List<Ref<EntityStore>> filterEntitiesInBox(Store<EntityStore> store, List<Ref<EntityStore>> entities, Box box) {
		var corrected = correctBoxBounds(box);
		var center = getBoxCenter(corrected);
		var normalized = new Box(new Vector3d(corrected.min).sub(center), new Vector3d(corrected.max).sub(center));
		var filtered = entities.stream().filter(e -> {
			TransformComponent transformComponent = store.getComponent(e, TransformComponent.getComponentType());
			BoundingBox boundingBoxComponent = store.getComponent(e, BoundingBox.getComponentType());
			if(transformComponent != null) {
				if(boundingBoxComponent != null) {
					return CollisionMath.isOverlapping(CollisionMath.intersectAABBs(transformComponent.getPosition(), boundingBoxComponent.getBoundingBox(), center, normalized));
				}
			}
			return false;
		});
		return filtered.toList();
	}

	public static List<Ref<EntityStore>> filterEntitiesInBox(Store<EntityStore> store, List<Ref<EntityStore>> entities, Vector3d centerPos, Box boundingBox) {
		var filtered = entities.stream().filter(e -> {
			TransformComponent transformComponent = store.getComponent(e, TransformComponent.getComponentType());
			BoundingBox boundingBoxComponent = store.getComponent(e, BoundingBox.getComponentType());
			if(transformComponent != null) {
				if(boundingBoxComponent != null) {
					return CollisionMath.isOverlapping(CollisionMath.intersectAABBs(transformComponent.getPosition(), boundingBoxComponent.getBoundingBox(), centerPos, boundingBox));
				}
			}
			return false;
		});
		return filtered.toList();
	}

	public static Box correctBoxBounds(Box box) {
		return new Box(Math.min(box.max.x, box.min.x), Math.min(box.max.y, box.min.y), Math.min(box.max.z, box.min.z), Math.max(box.max.x, box.min.x), Math.max(box.max.y, box.min.y), Math.max(box.max.z, box.min.z));
	}

	public static Vector3d getBoxCenter(Box box) {
		var corrected = correctBoxBounds(box);
		return new Vector3d(corrected.min.x + (corrected.width() / 2d), corrected.min.y + (corrected.height() / 2d), corrected.min.z + (corrected.depth() / 2d));
	}
}
