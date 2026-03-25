package com.diamssword.redCrystal.network;

import com.diamssword.redCrystal.RedCrystalPlugin;
import com.diamssword.redCrystal.display.ModelUtils;
import com.diamssword.redCrystal.display.RedEntityHiddenComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.entities.EntityUpdates;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
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
import java.util.logging.Level;

public class NetworkUtil {
	public static Set<Ref<EntityStore>> getVisibleEntities(Ref<EntityStore> playerRef) {
		Store<EntityStore> store = playerRef.getStore();
		EntityTrackerSystems.EntityViewer viewer = store.getComponent(playerRef, EntityTrackerSystems.EntityViewer.getComponentType());
		return (viewer != null) ? viewer.visible : Collections.emptySet();
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

	public static boolean setRuneVisibility(Ref<EntityStore> entityRef, Ref<EntityStore> playerRef, boolean visible) {
		if(entityRef.isValid() && entityRef.getStore().isInThread()) {
			var hidd = entityRef.getStore().getComponent(entityRef, RedEntityHiddenComponent.getComponentType());
			if(hidd != null) {
				if(visible) {
					hidd.addSeeingPlayer(playerRef);
					try {
						ModelComponent modelComponent = entityRef.getStore().getComponent(entityRef, ModelComponent.getComponentType());
						if(modelComponent != null) {
							var hasInteract = entityRef.getStore().getComponent(entityRef, Interactable.getComponentType()) != null;
							Model model;
							if(hasInteract)
								model = ModelUtils.withAttachment(modelComponent.getModel(), new ModelAttachment("Items/RedCrystal/Glyphs/HitboxHighlight.blockymodel", "Items/RedCrystal/Glyphs/HitboxHighlight.png", null, null, 1));
							else
								model = new Model(modelComponent.getModel());
							model = ModelUtils.withModel(model, hidd.getCurrentModel(), model.getTexture());
							ModelUpdate update = new ModelUpdate();
							update.model = model.toPacket();
							update.entityScale = hidd.getVisibleScale();
							NetworkUtil.sendEntityComponentUpdateToPlayer(playerRef, entityRef, new ComponentUpdateType[]{ComponentUpdateType.Intangible}, new ComponentUpdate[]{update});
							return true;
						}
					} catch(Exception exception) {RedCrystalPlugin.LOGGER.at(Level.WARNING).log(exception.getMessage());}
				} else {
					hidd.removeSeeingPlayer(playerRef);
					ModelComponent modelComponent = entityRef.getStore().getComponent(entityRef, ModelComponent.getComponentType());
					if(modelComponent != null) {
						Model model = ModelUtils.withAttachment(modelComponent.getModel());
						model = ModelUtils.withModel(model, hidd.getCurrentModel(), model.getTexture());
						ModelUpdate update = new ModelUpdate();
						update.model = model.toPacket();
						update.entityScale = hidd.getHiddenScale();
						NetworkUtil.sendEntityComponentUpdateToPlayer(playerRef, entityRef, null, new ComponentUpdate[]{update, new IntangibleUpdate()});
						return true;
					}
				}
			}
		}
		return false;
	}
}
