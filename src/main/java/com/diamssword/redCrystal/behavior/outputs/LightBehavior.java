package com.diamssword.redCrystal.behavior.outputs;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.display.ModelUtils;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Map;

public class LightBehavior extends RedCompBehavior<BehaviorAssetWithSettings.BehaviorAssetLight> {


	public LightBehavior(String id, RedElement parent, BehaviorAssetWithSettings.BehaviorAssetLight asset) {
		super(id, parent, asset);
	}

	@Override
	public void onSignalChange(short input, short oldValue, short value) {

		var ent = this.parent.getEntities().getOther("light");
		var light = getLightFromValue(value);
		System.out.println(light.red);
		if(ent != null && ent.isValid()) {
			var model = ent.getStore().getComponent(ent, ModelComponent.getComponentType());
			if(model != null)
				execute(() -> {
					ent.getStore().ensureComponent(ent, MovementStatesComponent.getComponentType());
					ModelParticle[] particles = new ModelParticle[0];
					System.out.println(light);
					if(light.red > 0 || light.blue > 0 || light.green > 0) {
						var col = asset.isRGB ? computeRGBColor() : computeColor(asset.light, value);
						var scale = asset.isRGB ? Math.max(getInputState(0), Math.max(getInputState(1), getInputState(2))) / (float) MAX : value / (float) MAX;
						particles = new ModelParticle[asset.particles.length];
						for(int i = 0; i < asset.particles.length; i++) {
							var base = asset.particles[i];
							if(base != null) {
								particles[i] = new ModelParticle(base.getSystemId(), base.getTargetEntityPart(), base.getTargetNodeName(), col, base.getScale() * scale, base.getPositionOffset(), base.getRotationOffset(), base.isDetachedFromModel());
							}
						}
					}
					ent.getStore().putComponent(ent, ModelComponent.getComponentType(), new ModelComponent(ModelUtils.withLight(model.getModel(), light, particles)));
				});

		}
	}

	public Color computeRGBColor() {
		// Clamp inputs (0–255)
		int R = Math.max(0, Math.min(255, getInputState(0)));
		int G = Math.max(0, Math.min(255, getInputState(1)));
		int B = Math.max(0, Math.min(255, getInputState(2)));

		int gray = 128;

		// Normalize each channel (0 → 1)
		float tR = R / 255f;
		float tG = G / 255f;
		float tB = B / 255f;

		// Interpolate each channel independently
		int finalR = (int) (gray + tR * (R - gray));
		int finalG = (int) (gray + tG * (G - gray));
		int finalB = (int) (gray + tB * (B - gray));

		return new Color((byte) finalR, (byte) finalG, (byte) finalB);
	}

	protected ColorLight getLightFromValue(float value) {

		if(this.asset.isRGB) {
			var r = (getInputState(0) / (float) MAX) * 2;
			var g = (getInputState(1) / (float) MAX) * 2;
			var b = (getInputState(2) / (float) MAX) * 2;
			return new ColorLight(this.asset.light.radius, (byte) (15 * r), (byte) (15 * g), (byte) (15 * b));
		} else {
			var fac = (value / (float) MAX) * 2; //looks better and brighter
			return new ColorLight(this.asset.light.radius, (byte) (this.asset.light.red * fac), (byte) (this.asset.light.green * fac), (byte) (this.asset.light.blue * fac));
		}
	}

	public static Color computeColor(ColorLight light, float value) {
		float t = (value - MIN) / (MAX - MIN);
		t = Math.max(0, Math.min(1, t));

		int targetR = light.red * 17;
		int targetG = light.green * 17;
		int targetB = light.blue * 17;

		int gray = 128;
		byte finalR = (byte) (gray + t * (targetR - gray));
		byte finalG = (byte) (gray + t * (targetG - gray));
		byte finalB = (byte) (gray + t * (targetB - gray));

		return new Color(finalR, finalG, finalB);
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
