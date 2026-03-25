package com.diamssword.redCrystal.behavior;

import com.diamssword.redCrystal.display.ModelUtils;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.StateLoader;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Map;

public class LightBehavior extends RedCompBehavior<BehaviorAssetWithSettings.BehaviorAssetParticle> {

	public LightBehavior(String id, RedElement parent, BehaviorAssetWithSettings.BehaviorAssetParticle asset) {
		super(id, parent, asset);
	}

	@Override
	void onSignalChange(short input, short oldValue, short value) {

		var ent = this.parent.getEntities().getOther("light");
		if(ent != null && ent.isValid()) {
			var model = ent.getStore().getComponent(ent, ModelComponent.getComponentType());
			if(model != null)
				execute(() -> {
					ent.getStore().ensureComponent(ent, MovementStatesComponent.getComponentType());
					byte power = (byte) (value * 0.1);
					ModelParticle[] particles = new ModelParticle[0];
					if(power > 0) {
						var col = RedComponentDisplayUtils.redColorFromShort(value);
						particles = new ModelParticle[asset.particles.length];
						for(int i = 0; i < asset.particles.length; i++) {
							var base = asset.particles[i];
							if(base != null) {
								particles[i] = new ModelParticle(base.getSystemId(), base.getTargetEntityPart(), base.getTargetNodeName(), col, base.getScale() * (value / (float) MAX), base.getPositionOffset(), base.getRotationOffset(), base.isDetachedFromModel());
							}
						}
					}
					ent.getStore().putComponent(ent, ModelComponent.getComponentType(), new ModelComponent(ModelUtils.withLight(model.getModel(), new ColorLight((byte) 0, power, power, power), particles)));
				});

		}
	}

	@NullableDecl
	@Override
	public Map<String, Holder<EntityStore>> displayEntities(EntityStore store, BlockFace facing) {
		var res = super.displayEntities(store, facing);
		var holder = RedComponentDisplayUtils.createMinimalDisplayEntity(store, parent.getParent().getPosition(), facing);
		//holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(new Box(0, 0, 0, 1, 1, 1)));
		holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(0.00001f));
		holder.ensureComponent(Intangible.getComponentType());
		holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(RedComponentDisplayUtils.getFlatModel(facing)));
		holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent("light", (short) 0, this.parent));
		res.put("light", holder);
		return res;
	}

}
