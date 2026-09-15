package com.diamssword.redCrystal.storage.assets;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorSectionStart;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.MapUtil;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class BehaviorAssetWithInteraction extends AbstractBehaviorAsset<BehaviorAssetWithInteraction> {
	public static BuilderCodec<BehaviorAssetWithInteraction> Codec(String id) {
		return BuilderCodec.builder(BehaviorAssetWithInteraction.class, () -> new BehaviorAssetWithInteraction(id))
				.appendInherited(
						new KeyedCodec<>("Interactions", new EnumMapCodec<>(InteractionType.class, RootInteraction.CHILD_ASSET_CODEC)),
						(item, v) -> item.interactions = MapUtil.combineUnmodifiable(item.interactions, v, () -> new EnumMap<>(InteractionType.class)),
						item -> item.interactions,
						(item, parent) -> item.interactions = parent.interactions
				).addValidator(RootInteraction.VALIDATOR_CACHE.getMapValueValidator())
				.metadata(new UIEditorSectionStart("Interactions")).add()
				.appendInherited(
						new KeyedCodec<>("InteractionVars", new MapCodec<>(RootInteraction.CHILD_ASSET_CODEC, HashMap::new)),
						(item, v) -> item.interactionVars = MapUtil.combineUnmodifiable(item.interactionVars, v),
						item -> item.interactionVars,
						(item, parent) -> item.interactionVars = parent.interactionVars
				)
				.addValidator(RootInteraction.VALIDATOR_CACHE.getMapValueValidator())
				.add()
				.appendInherited(
						new KeyedCodec<>("Cooldown", Codec.FLOAT),
						(item, v) -> item.delay = v,
						item -> item.delay,
						(item, parent) -> item.delay = parent.delay
				)
				.addValidator(Validators.greaterThanOrEqual(0f)).add().build();
	}

	public Map<InteractionType, String> interactions = Collections.emptyMap();
	public float delay = 1f;
	public Map<String, String> interactionVars = Collections.emptyMap();

	public BehaviorAssetWithInteraction(String id) {
		super(id);
	}
}