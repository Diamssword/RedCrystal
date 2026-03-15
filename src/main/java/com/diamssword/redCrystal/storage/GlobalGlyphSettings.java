package com.diamssword.redCrystal.storage;

import com.diamssword.redCrystal.display.RedEntityHiddenComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import org.jetbrains.annotations.NotNull;

public class GlobalGlyphSettings {
	public static final BuilderCodec<GlobalGlyphSettings> CODEC = BuilderCodec.builder(GlobalGlyphSettings.class, GlobalGlyphSettings::new)
			.append(new KeyedCodec<>("Visibility", new TypedEnumCodec<>(RedEntityHiddenComponent.Visibility.class)),
					(item, b) -> item.glyphVisibility = b,
					item -> item.glyphVisibility)
			.documentation("Set Glyph's visiblity:\n -Hidden:Shown only when powered\n -Visible:Always shown\n -Pulse:Only Show signal changing\n -Invisible:Never shown")
			.add()
			.build();
	private RedEntityHiddenComponent.Visibility glyphVisibility = RedEntityHiddenComponent.Visibility.Hidden;

	public GlobalGlyphSettings() {}

	public void updateFrom(GlobalGlyphSettings settings, RedElement element) {

		if(this.glyphVisibility != settings.glyphVisibility) {
			element.setAsset(element.getAsset());
		}
	}

	public RedEntityHiddenComponent.Visibility getVisibility() {
		return glyphVisibility;
	}


	public static class TypedEnumCodec<T extends Enum<T>> extends EnumCodec<T> {
		public final Class<T> clazz;

		public TypedEnumCodec(@NotNull Class<T> clazz) {
			super(clazz);
			this.clazz = clazz;
		}
	}
}
