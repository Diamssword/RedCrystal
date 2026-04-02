package com.diamssword.redCrystal.worldInteraction;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.collision.CollisionMath;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;

public class CollideUtil {

	public static boolean isEntityInBox(Ref<EntityStore> entity, Box box) {
		return !filterEntitiesInBox(entity.getStore(), List.of(entity), box).isEmpty();
	}

	public static List<Ref<EntityStore>> filterEntitiesInBox(Store<EntityStore> store, List<Ref<EntityStore>> entities, Box box) {
		var corrected = correctBoxBounds(box);
		var center = getBoxCenter(corrected);
		var normalized = new Box(corrected.min.clone().subtract(center.clone()), corrected.max.clone().subtract(center.clone()));
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
