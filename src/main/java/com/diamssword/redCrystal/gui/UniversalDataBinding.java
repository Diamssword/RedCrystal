package com.diamssword.redCrystal.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class UniversalDataBinding implements EventDataWithGlyphSettings {
	static final String KEY_ID = "ElementId";
	static final String KEY_TYPE = "ValueType";
	static final String KEY_VALUE_S = "@ElementValueS";
	static final String KEY_VALUE_D = "@ElementValueD";
	static final String KEY_VALUE_B = "@ElementValueB";
	public static final String KEY_VALUE_I = "@ElementValueI";
	public static final BuilderCodec<UniversalDataBinding> CODEC = appendFields(BuilderCodec.builder(
			UniversalDataBinding.class, UniversalDataBinding::new
	)).build();

	public static <T extends EventDataWithGlyphSettings> BuilderCodec.Builder<T> appendFields(BuilderCodec.Builder<T> codec) {
		return codec.append(new KeyedCodec<>(KEY_ID, Codec.STRING), (entry, s) -> entry.getSettings().elementId = s, entry -> entry.getSettings().elementId)
				.add()
				.append(new KeyedCodec<>(KEY_TYPE, Codec.STRING), (entry, s) -> entry.getSettings().type = s, entry -> entry.getSettings().type)
				.add()
				.append(new KeyedCodec<>(KEY_VALUE_S, Codec.STRING), (entry, s) -> entry.getSettings().stringValue = s, entry -> entry.getSettings().stringValue)
				.add()
				.append(new KeyedCodec<>(KEY_VALUE_D, Codec.DOUBLE), (entry, s) -> entry.getSettings().doubleValue = s, entry -> entry.getSettings().doubleValue)
				.add()
				.append(new KeyedCodec<>(KEY_VALUE_B, Codec.BOOLEAN), (entry, s) -> entry.getSettings().booleanValue = s, entry -> entry.getSettings().booleanValue)
				.add()
				.append(new KeyedCodec<>(KEY_VALUE_I, Codec.INTEGER), (entry, s) -> entry.getSettings().integerValue = s, entry -> entry.getSettings().integerValue)
				.add();
	}

	public String elementId;
	public String type;
	public String stringValue;
	public double doubleValue;
	public int integerValue;
	public boolean booleanValue;

	public UniversalDataBinding() {
	}

	@Override
	public UniversalDataBinding getSettings() {
		return this;
	}

}


