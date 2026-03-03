package com.diamssword.redCrystal.network;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ComponentUpdate;
import com.hypixel.hytale.protocol.ComponentUpdateType;
import com.hypixel.hytale.protocol.EntityUpdate;
import com.hypixel.hytale.protocol.packets.entities.EntityUpdates;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.receiver.IPacketReceiver;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class NetworkUtil {
	public static Set<Ref<EntityStore>> getVisibleEntities(Ref<EntityStore> playerRef) {
		Store<EntityStore> store = playerRef.getStore();
		EntityTrackerSystems.EntityViewer viewer = store.getComponent(playerRef, EntityTrackerSystems.EntityViewer.getComponentType());
		return (viewer != null) ? viewer.visible : Collections.<Ref<EntityStore>>emptySet();
	}


	public static List<PlayerRef> getPlayersInView(Ref<EntityStore> entity) {
		List<PlayerRef> players = new ArrayList<>();
		EntityTrackerSystems.Visible visible = entity.getStore().getComponent(entity, EntityModule.get().getVisibleComponentType());
		if(visible != null) {
			for(Ref<EntityStore> viewerRef : visible.visibleTo.keySet()) {
				PlayerRef pRef = entity.getStore().getComponent(viewerRef, PlayerRef.getComponentType());
				if(pRef != null) players.add(pRef);
			}
		}
		return players;
	}

	public static void sendEntityComponentUpdateToPlayer(Ref<EntityStore> playerRef, Ref<EntityStore> entityRef, @Nullable ComponentUpdateType[] removed, ComponentUpdate[] updates) {
		assert playerRef != null && entityRef != null && updates != null && updates.length > 0;
		try {
			NetworkId networkIdComponent = entityRef.getStore().getComponent(entityRef, NetworkId.getComponentType());
			assert networkIdComponent != null;
			int networkId = networkIdComponent.getId();
			EntityTrackerSystems.EntityViewer viewerComponent = playerRef.getStore().getComponent(playerRef, EntityTrackerSystems.EntityViewer.getComponentType());
			assert viewerComponent != null;
			IPacketReceiver packetReceiver = viewerComponent.packetReceiver;
			EntityUpdate entityUpdate = new EntityUpdate(networkId, removed, updates);
			EntityUpdates packet = new EntityUpdates();
			packet.removed = null;
			packet.updates = new EntityUpdate[]{entityUpdate};
			packetReceiver.writeNoCache(packet);
		} catch(Exception e) {
			System.err.println("Failed to send EntityEffects packet: " + e.getMessage());
		}
	}
}
