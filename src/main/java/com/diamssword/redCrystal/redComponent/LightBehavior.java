package com.diamssword.redCrystal.redComponent;

import com.diamssword.redCrystal.display.ModelUtils;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;

public class LightBehavior extends RedCompBehavior<BehaviorAssetWithSettings.BehaviorAssetParticle> {

	public LightBehavior(String id, RedElement parent, BehaviorAssetWithSettings.BehaviorAssetParticle asset) {
		super(id, parent, asset);
	}

	@Override
	void onSignalChange(short input, short oldValue, short value) {

		var ent = this.parent.getEntities().getMain();
		if(ent != null && ent.isValid()) {
			lightUpRune(ent, value > MIN);
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

}
