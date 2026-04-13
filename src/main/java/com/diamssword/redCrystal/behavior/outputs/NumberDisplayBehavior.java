package com.diamssword.redCrystal.behavior.outputs;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.display.ModelUtils;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
import com.hypixel.hytale.component.Holder;
import org.joml.Vector2d;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Map;

public class NumberDisplayBehavior extends RedCompBehavior<BehaviorAsset> {


	public NumberDisplayBehavior(String id, RedElement parent, BehaviorAsset asset) {
		super(id, parent, asset);
	}

	@Override
	public void onSignalChange(short input, short oldValue, short value) {

		var ent = this.parent.getEntities().getOther("display");
		if(ent != null && ent.isValid()) {
			execute(() -> {
				ent.getStore().getComponent(ent, Nameplate.getComponentType()).setText(value + "");
			});

		}
	}

	@NullableDecl
	@Override
	public Map<String, Holder<EntityStore>> displayEntities(EntityStore store, BlockFace facing) {
		var res = super.displayEntities(store, facing);
		var holder = RedComponentDisplayUtils.createMinimalDisplayEntity(store, parent.getParent().getPosition(), facing, new Vector2d(0, facing != BlockFace.Up && facing != BlockFace.Down ? -0.45 : 0));
		if(facing == BlockFace.Down || facing == BlockFace.Up) {
			var trans = holder.getComponent(TransformComponent.getComponentType());
			if(facing == BlockFace.Up)
				trans.getPosition().add(0, -0.35f, 0);
			else
				trans.getPosition().add(0, -0.55f, 0);
		}
		//holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(new Box(0, 0, 0, 1, 1, 1)));
		holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(0.00001f));
		holder.ensureComponent(Intangible.getComponentType());
		holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(ModelUtils.withTexture(RedComponentDisplayUtils.getFlatModel(facing), "Items/RedCrystal/Glyphs/Empty.png")));
		holder.addComponent(RedEntityLinkComponent.getComponentType(), new RedEntityLinkComponent("display", (short) 0, this.parent));
		holder.addComponent(Nameplate.getComponentType(), new Nameplate(getInputState(0) + ""));
		res.put("display", holder);
		return res;
	}

}
