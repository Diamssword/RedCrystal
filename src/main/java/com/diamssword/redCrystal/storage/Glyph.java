package com.diamssword.redCrystal.storage;

import com.diamssword.redCrystal.storage.assets.AbstractBehaviorAsset;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.*;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTranslationProperties;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Glyph implements JsonAssetWithMap<String, DefaultAssetMap<String, Glyph>>, Comparable<Glyph> {
	public static final CommonAssetValidator ICON_GLYPH_VALIDATOR = new CommonAssetValidator("png", "UI/Custom");
	public static final Map<String, Integer> CategoryWheight = Map.of("Rune", 100, "Hex", 99, "Sigil", 98);
	private static final AssetBuilderCodec.Builder<String, Glyph> CODEC_BUILDER = AssetBuilderCodec.builder(
					Glyph.class,
					Glyph::new,
					Codec.STRING,
					(item, blockTypeKey) -> item.id = blockTypeKey,
					item -> item.id,
					(asset, data) -> asset.data = data,
					asset -> asset.data
			)
			.appendInherited(
					new KeyedCodec<>("TranslationProperties", ItemTranslationProperties.CODEC),
					(item, s) -> item.translationProperties = s,
					item -> item.translationProperties,
					(item, parent) -> item.translationProperties = parent.translationProperties
			)
			.documentation("The translation properties for this glyph asset.")
			.add()
			.appendInherited(new KeyedCodec<>("Icon", Codec.STRING), (item, s) -> item.icon = s, item -> item.icon, (item, parent) -> item.icon = parent.icon)
			.addValidator(ICON_GLYPH_VALIDATOR)
			.documentation("The glyph's icon used in UIs.")
			.add()
			.appendInherited(
					new KeyedCodec<>("Inputs", Codec.SHORT), (item, s) -> item.inputs = s, item -> item.inputs, (item, parent) -> item.inputs = parent.inputs
			)
			.metadata(new UIPropertyTitle("Inputs Count"))
			.documentation("The number of inputs this glyph have")
			.addValidator(Validators.greaterThan((short) -1))
			.add()
			.appendInherited(
					new KeyedCodec<>("Outputs", Codec.SHORT), (item, s) -> item.outputs = s, item -> item.outputs, (item, parent) -> item.outputs = parent.outputs
			)
			.metadata(new UIPropertyTitle("Outputs Count"))
			.documentation("The number of outputs this glyph have")
			.addValidator(Validators.greaterThan((short) -1))
			.add()
			.appendInherited(
					new KeyedCodec<>("BehaviorId", AbstractBehaviorAsset.BEHAVIOR_CODEC),
					(o, i) -> o.behavior = i,
					o -> o.behavior,
					(o, p) -> o.behavior = p.behavior
			)
			.addValidator(Validators.nonNull())
			.documentation("The Id of the Behavior to use")
			.add()
			.appendInherited(
					new KeyedCodec<>("GlyphTexture", Codec.STRING), (item, s) -> item.texture = s, item -> item.texture, (item, parent) -> item.texture = parent.texture
			)
			.addValidator(CommonAssetValidator.TEXTURE_CHARACTER)
			.metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
			.metadata(new UIPropertyTitle("Glyph Texture"))
			.documentation("The texture used for rendering the glyph into the world. Must be an entity texture")
			.add();

	public static final AssetCodec<String, Glyph> CODEC = CODEC_BUILDER.build();
	private String id;
	private AssetExtraInfo.Data data;
	private ItemTranslationProperties translationProperties;
	private short inputs = 0;
	private short outputs = 0;
	private String texture;
	private String icon;
	private AbstractBehaviorAsset behavior;

	protected Glyph() {}

	public Glyph(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	public AssetExtraInfo.Data getData() {
		return this.data;
	}

	public ItemTranslationProperties getTranslationProperties() {
		return translationProperties;
	}

	public Glyph setTranslationProperties(ItemTranslationProperties translationProperties) {
		this.translationProperties = translationProperties;
		return this;
	}

	public short getInputs() {
		return inputs;
	}

	public Glyph setInputs(short inputs) {
		this.inputs = inputs;
		return this;
	}

	public short getOutputs() {
		return outputs;
	}

	public Glyph setOutputs(short outputs) {
		this.outputs = outputs;
		return this;
	}

	public String getTexture() {
		return texture;
	}

	public Glyph setTexture(String texture) {
		this.texture = texture;
		return this;
	}

	public String getIcon() {
		return icon;
	}

	public Glyph setIcon(String icon) {
		this.icon = icon;
		return this;
	}

	public AbstractBehaviorAsset getBehavior() {
		return behavior;
	}

	public Glyph setBehavior(AbstractBehaviorAsset behavior) {
		this.behavior = behavior;
		return this;
	}

	public String getCategorie() {
		var tags = this.getData().getRawTags().get("Type");
		if(tags != null && tags.length > 0)
			return tags[tags.length - 1];
		return "";
	}

	@Override
	public int compareTo(@NotNull Glyph other) {
		var cat = getCategorie();
		var catO = other.getCategorie();
		// 1. Compare weight (category priority)

		int weightCompare = Integer.compare(CategoryWheight.getOrDefault(catO, 0), CategoryWheight.getOrDefault(cat, 0));
		if(weightCompare != 0) {
			return weightCompare;
		}

		// 2. Compare type (grouping)
		return cat.compareTo(catO);
	}
}
