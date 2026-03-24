package com.diamssword.redCrystal.behavior;

import com.diamssword.redCrystal.storage.DisplayState;
import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.diamssword.redCrystal.worldInteraction.FakeLivingEntity;
import com.diamssword.redCrystal.display.RedComponentDisplayUtils;
import com.diamssword.redCrystal.storage.RedElement;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.InteractionSimulationHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Map;

public class InteractBehavior extends RedCompBehavior<BehaviorAsset> {

	public InteractBehavior(String id, RedElement parent, BehaviorAsset asset) {
		super(id, parent, asset);
	}

	@NullableDecl
	@Override
	public Map<String, Holder<EntityStore>> displayEntities(EntityStore store, BlockFace facing) {
		var res = super.displayEntities(store, facing);
		var holder = RedComponentDisplayUtils.createMinimalDisplayEntity(store, parent.getParent().getPosition(), facing);
		//var model = RedComponentDisplayUtils.modifyBoundingBox(Model.createScaledModel(ModelAsset.getAssetMap().getAsset("RedCrystal_Button"), 0.5f), facing);
		holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(new Box(0, 0, 0, 1, 1, 1)));
		holder.ensureComponent(Intangible.getComponentType());
		var ent = new FakeLivingEntity();
		holder.addComponent(FakeLivingEntity.getElementType(), ent);
		holder.putComponent(InteractionModule.get().getInteractionManagerComponent(), new InteractionManager(ent, null, new InteractionSimulationHandler()));
		res.put("interactor", holder);
		return res;
	}

	@Override
	void setLightState(DisplayState display) {
		System.out.println("light change");
		display.setMain(display.isAnyInputOn());
	}

	@Override
	void onSignalChange(short input, short oldValue, short value) {
		if(oldValue == value)
			return;
		this.parent.getParent().getChunkRef().getStore().getExternalData().getWorld().sendMessage(Message.raw("Signal is " + value));

		var block = getWorld().getBlockType(this.parent.getParent().getPosition());
		var map = block.getInteractions();
		if(map != null && map.containsKey(InteractionType.Use)) {

			RootInteraction rootInteraction = RootInteraction.getAssetMap().getAsset(map.get(InteractionType.Use));
			if(rootInteraction != null) {
				var ent = this.parent.getEntities().getOther("interactor");
				if(ent != null && ent.isValid()) {
					var manager = ent.getStore().getComponent(ent, InteractionModule.get().getInteractionManagerComponent());
					InteractionContext ctx = InteractionContext.forInteraction(manager, ent, InteractionType.Use, ent.getStore());
					var pos = this.parent.getParent().getPosition();
					ctx.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK, new BlockPosition(pos.x, pos.y, pos.z));
					getWorld().execute(() -> {
						try {
							var cb = new CommandBuffer(ent.getStore()) {};
							manager.tryStartChain(ent, cb, InteractionType.Use, ctx, rootInteraction);
						} catch(Exception e) {
							e.printStackTrace();
						}
					});
				}

			}
		}


	}

}
