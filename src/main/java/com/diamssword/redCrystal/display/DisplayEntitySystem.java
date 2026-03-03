package com.diamssword.redCrystal.display;

import com.diamssword.redCrystal.network.NetworkUtil;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.ComponentUpdate;
import com.hypixel.hytale.protocol.ModelUpdate;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class DisplayEntitySystem extends EntityTickingSystem<EntityStore> {


	@Override
	public Query<EntityStore> getQuery() {
		return RedEntityHiddenComponent.getComponentType();
	}

	@Override
	public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
		var hidden = archetypeChunk.getComponent(index, RedEntityHiddenComponent.getComponentType());
		if(hidden != null && hidden.parent.isValid()) {
			hidden.getSeeingPlayers().forEach(player -> {
				if(player.getReference() != null) {

					var MaxO = hidden.parent.getBehavior().maxOutputs();
					for(short i = 0; i < MaxO; i++) {
						var output = hidden.parent.getOuput(i);
						if(output != null) {
							var vec1 = RedComponentDisplayUtils.getIOPosition(i, hidden.parent.getBehavior(), true);
							var linked = output.getBehavior(hidden.parent.getParent());
							if(linked != null) {
								var vec2 = RedComponentDisplayUtils.getIOPosition(output.getInputIndex(), linked, false);
								RedComponentDisplayUtils.drawLaserFor(vec1, vec2, 0.1f, 0x5050BB, player.getReference());
							}
						}
					}
				}
			});
			if(hidden.needLightUpSync()) {
				if(hidden.isLightUp()) {
					lightRune(archetypeChunk.getReferenceTo(index), hidden);
					hidden.setLightUpState(2);
				} else {
					hidden.setLightUpState(0);
					hideRune(archetypeChunk.getReferenceTo(index), hidden);
				}
			}
		}
	}

	public void lightRune(Ref<EntityStore> rune, RedEntityHiddenComponent comp) {
		var store = rune.getStore();
		var modelComponent = store.getComponent(rune, ModelComponent.getComponentType());
		if(modelComponent != null) {
			try {
				Model model = RedComponentDisplayUtils.withModel(modelComponent.getModel(), "Items/RedCrystal/Glyphs/Flat_Glow.blockymodel", modelComponent.getModel().getTexture());
				ModelUpdate update = new ModelUpdate();
				update.model = model.toPacket();
				update.entityScale = comp.baseScale;
				NetworkUtil.getPlayersInView(rune).forEach(e -> {
					NetworkUtil.sendEntityComponentUpdateToPlayer(e.getReference(), rune, null, new ComponentUpdate[]{update});
				});

			} catch(Exception exception) {}
		}
	}

	public void hideRune(Ref<EntityStore> rune, RedEntityHiddenComponent comp) {
		var store = rune.getStore();
		var modelComponent = store.getComponent(rune, ModelComponent.getComponentType());
		if(modelComponent != null) {
			try {
				Model model = RedComponentDisplayUtils.withModel(modelComponent.getModel(), "Items/RedCrystal/Glyphs/Flat.blockymodel", modelComponent.getModel().getTexture());
				ModelUpdate update = new ModelUpdate();
				update.model = model.toPacket();
				update.entityScale = 0.00001f;
				NetworkUtil.getPlayersInView(rune).forEach(e -> {
					NetworkUtil.sendEntityComponentUpdateToPlayer(e.getReference(), rune, null, new ComponentUpdate[]{update});
				});

			} catch(Exception exception) {}
		}
	}
}
