package com.diamssword.redCrystal.storage;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.item.config.metadata.AdventureMetadata;

public class RedWandStorage {
	public static final BuilderCodec<RedWandStorage> CODEC = BuilderCodec.builder(RedWandStorage.class, RedWandStorage::new)
			.appendInherited(
					new KeyedCodec<>("Glyph", Codec.STRING),
					(meta, s) -> meta.selectedGlyph = s,
					meta -> meta.selectedGlyph,
					(meta, parent) -> meta.selectedGlyph = parent.selectedGlyph
			)
			.add()
			.build();
	private String selectedGlyph;

	public RedWandStorage() {

	}

	public String getSelectedGlyph() {
		return selectedGlyph;
	}

	public void setSelectedGlyph(String selectedGlyph) {
		this.selectedGlyph = selectedGlyph;
	}
}
