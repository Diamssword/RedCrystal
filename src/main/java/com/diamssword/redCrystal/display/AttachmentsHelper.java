package com.diamssword.redCrystal.display;

import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.cosmetics.CosmeticRegistry;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkinPart;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class AttachmentsHelper {
	static CosmeticRegistry reg = CosmeticsModule.get().getRegistry();

	public static void addAttachment(ArrayList<ModelAttachment> attachments, PlayerSkinPart part, @Nullable String gradientId, @Nullable PlayerSkinPart.Variant variant, @Nullable String texture) {
		attachments.add(
				new ModelAttachment(
						variant != null ? variant.getModel() : part.getModel(),
						texture != null ? texture : (variant != null ? variant.getGreyscaleTexture() : part.getGreyscaleTexture()),
						part.getGradientSet(),
						gradientId,
						1.0
				)
		);
	}

	public static List<ModelAttachment> parseSkin(PlayerSkin skin, @Nullable ArrayList<String> ignore, @Nullable String defaultGradientId) {
		ArrayList<ModelAttachment> attachments = new ArrayList<>();

		// We go through all the fields of the class PlayerSkin
		for(Field skinField : skin.getClass().getDeclaredFields()) {
			// We only need string fields so skipping all other
			if(skinField.getType() != String.class) continue;

			// Skip ignored fields
			if(ignore != null && ignore.contains(skinField.getName())) continue;

			// Collecting skin part data
			String skinPartValue;
			try {
				skinPartValue = (String) skinField.get(skin);
			} catch(IllegalAccessException ignored) {
				continue;
			}
			if(skinPartValue == null) continue;

			// Converting field name to enum
			String upperSnakeName = skinField.getName().replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
			CosmeticType cosmeticType;
			try {
				cosmeticType = CosmeticType.valueOf(upperSnakeName);
			} catch(IllegalArgumentException e) {
				String enumName = upperSnakeName + "S";
				cosmeticType = CosmeticType.valueOf(enumName);
			}
			// 0 - part id; 1 - gradient id; 2 - variant
			String[] cosmeticParts = skinPartValue.split("\\.");

			PlayerSkinPart skinPart = (PlayerSkinPart) reg.getByType(cosmeticType).get(cosmeticParts[0]);

			if(cosmeticType == CosmeticType.HEAD_ACCESSORY)
				System.out.println(skinPart);
			// Collecting gradient id from parsed cosmetic data

			String gradientId;
			String textureId = null;
			if(cosmeticParts.length > 1) {

				gradientId = cosmeticParts[1];
				if(skinPart.getTextures() != null) {
					var vari = skinPart.getTextures().get(gradientId);

					if(vari != null) {
						textureId = vari.getTexture();
					}
				}
			} else {
				gradientId = defaultGradientId;
			}
			PlayerSkinPart.Variant skinPartVariant = null;
			if(cosmeticParts.length > 2) {

				skinPartVariant = skinPart.getVariants().get(cosmeticParts[2]);
			}

			addAttachment(attachments, skinPart, gradientId, skinPartVariant, textureId);
		}

		System.out.println(attachments);
		return attachments;
	}
}