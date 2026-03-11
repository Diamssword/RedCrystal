package com.diamssword.redCrystal.display;

import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;

public class ModelUtils {
	public static Model withBB(Model base, Box box) {
		return new Model(
				base.getModelAssetId(),
				base.getScale(),
				base.getRandomAttachmentIds(),
				base.getAttachments(),
				box,
				base.getModel(),
				base.getTexture(),
				base.getGradientSet(),
				base.getGradientId(),
				base.getEyeHeight(),
				base.getCrouchOffset(),
				base.getSittingOffset(),
				base.getSleepingOffset(),
				base.getAnimationSetMap(),
				base.getCamera(),
				base.getLight(),
				base.getParticles(),
				base.getTrails(),
				base.getPhysicsValues(),
				base.getDetailBoxes(),
				base.getPhobia(),
				base.getPhobiaModelAssetId()
		);
	}

	public static Model withAttachment(Model base, ModelAttachment... attachments) {
		return new Model(
				base.getModelAssetId(),
				base.getScale(),
				base.getRandomAttachmentIds(),
				attachments,
				base.getBoundingBox(),
				base.getModel(),
				base.getTexture(),
				base.getGradientSet(),
				base.getGradientId(),
				base.getEyeHeight(),
				base.getCrouchOffset(),
				base.getSittingOffset(),
				base.getSleepingOffset(),
				base.getAnimationSetMap(),
				base.getCamera(),
				base.getLight(),
				base.getParticles(),
				base.getTrails(),
				base.getPhysicsValues(),
				base.getDetailBoxes(),
				base.getPhobia(),
				base.getPhobiaModelAssetId()
		);
	}

	public static Model withTexture(Model base, String texture) {
		return new Model(
				base.getModelAssetId(),
				base.getScale(),
				base.getRandomAttachmentIds(),
				base.getAttachments(),
				base.getBoundingBox(),
				base.getModel(),
				texture,
				base.getGradientSet(),
				base.getGradientId(),
				base.getEyeHeight(),
				base.getCrouchOffset(),
				base.getSittingOffset(),
				base.getSleepingOffset(),
				base.getAnimationSetMap(),
				base.getCamera(),
				base.getLight(),
				base.getParticles(),
				base.getTrails(),
				base.getPhysicsValues(),
				base.getDetailBoxes(),
				base.getPhobia(),
				base.getPhobiaModelAssetId()
		);
	}

	public static Model withModel(Model base, String newModel, String texture) {
		return new Model(
				base.getModelAssetId(),
				base.getScale(),
				base.getRandomAttachmentIds(),
				base.getAttachments(),
				base.getBoundingBox(),
				newModel,
				texture,
				base.getGradientSet(),
				base.getGradientId(),
				base.getEyeHeight(),
				base.getCrouchOffset(),
				base.getSittingOffset(),
				base.getSleepingOffset(),
				base.getAnimationSetMap(),
				base.getCamera(),
				base.getLight(),
				base.getParticles(),
				base.getTrails(),
				base.getPhysicsValues(),
				base.getDetailBoxes(),
				base.getPhobia(),
				base.getPhobiaModelAssetId()
		);
	}

}
