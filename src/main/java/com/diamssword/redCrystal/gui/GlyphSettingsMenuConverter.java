package com.diamssword.redCrystal.gui;

import com.diamssword.redCrystal.storage.GlobalGlyphSettings;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.builder.BuilderField;
import com.hypixel.hytale.codec.codecs.simple.BooleanCodec;
import com.hypixel.hytale.codec.codecs.simple.DoubleCodec;
import com.hypixel.hytale.codec.codecs.simple.FloatCodec;
import com.hypixel.hytale.codec.codecs.simple.IntegerCodec;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.bson.BsonDocument;
import org.bson.BsonString;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class GlyphSettingsMenuConverter<T> {

	private final Consumer<T> setter;
	private final Supplier<T> getter;
	private final BuilderCodec<T> codec;
	private final String glyphId;
	private final UniversalEventBinder binder;

	public GlyphSettingsMenuConverter(UniversalEventBinder binder, String glyphId, PlayerRef playerRef, Supplier<T> settingsProvider, Consumer<T> settingsSetter, BuilderCodec<T> codec) {
		this.setter = settingsSetter;
		this.getter = settingsProvider;
		this.playerRef = playerRef;
		this.codec = codec;
		this.glyphId = glyphId;
		this.binder = binder;
	}

	private final PlayerRef playerRef;

	public void appendSettings(String key, UICommandBuilder builder) {
		codec.getEntries().forEach((k, v) -> {
			for(BuilderField<T, ?> f : v) {
				var str = codecFieldConverter(f);
				builder.appendInline("#" + key, str);
			}
		});
	}


	private String translate(String translateString) {
		var tr = I18nModule.get().getMessage(playerRef.getLanguage(), "server.RedCrystal.glyphSetting." + glyphId + "." + translateString);
		return tr != null ? tr : translateString;
	}

	private String getLabel(BuilderField<T, ?> field) {
		var doc = translate(field.getCodec().getKey() + ".desc");
		return new StringBuilder("Label{Text:\"").append(translate(field.getCodec().getKey() + ".name")).append("\"; Padding:(Right:5);TooltipText:\"").append(doc).append("\"; Style: (HorizontalAlignment:Center);}").toString();
	}

	private String codecFieldConverter(BuilderField<T, ?> field) {
		var div = new StringBuilder("Group{LayoutMode:Left; Padding:(Top:10,Bottom:10,Left:5,Right:5);Anchor: (Height:40); ");

		var doc = translate(field.getCodec().getKey() + ".desc");
		div.append(getLabel(field));
		StringBuilder content = new StringBuilder();
		switch(field.getCodec().getChildCodec()) {
			case BooleanCodec _ -> {
				var id = binder.bindEventBool((b, builder) -> {
					setValue(field, b);
				});
				content.append("CheckBox #").append(id).append("{ Padding: (Full: 4); Anchor: (Width: 22, Height: 22); Background: (TexturePath: \"Common/CheckBoxFrame.png\", Border: 7); Value:").append(getValue(field, Boolean.class).orElse(false)).append("; TooltipText:\"").append(doc).append("\";}");
				binder.setStyle(id, "DefaultCheckBoxStyle");
			}
			case GlobalGlyphSettings.TypedEnumCodec<?> ec -> {
				var values = ec.clazz.getEnumConstants();
				var id = binder.bindEvent((s, builder) -> {
					setValueE((BuilderField<T, Enum<?>>) field, ec.decode(new BsonString(s), new ExtraInfo()));
				});
				binder.setStyle(id, "DefaultDropdownBoxStyle");
				content.append("DropdownBox #").append(id).append(" {Anchor:(Width:180); TooltipText:\"").append(doc).append("\";");
				getValue(field, ec.clazz).ifPresent((v) -> content.append("Value:\"").append(v).append("\";"));
				for(Enum<? extends Enum<?>> value : values) {
					content.append("DropdownEntry{ Value:\"").append(value.toString()).append("\"; Text:\"").append(value).append("\";} ");
				}
				content.append("}");
			}
			case IntegerCodec _ -> {
				content.append(numberFieldsHandler(field, Integer.class, (d) -> setValue(field, d.intValue())));
			}
			case FloatCodec _ -> {
				content.append(numberFieldsHandler(field, Float.class, (d) -> setValue(field, d.floatValue())));

			}
			case DoubleCodec _ -> {
				content.append(numberFieldsHandler(field, Double.class, (d) -> setValue(field, d)));

			}
			case BuilderCodec builder -> {
				if(builder.getInnerClass() == ColorLight.class) {
					div = new StringBuilder("Group{LayoutMode:Left; Padding:(Top:10,Bottom:10,Left:5,Right:5); Anchor:(Left:0,Height:120); ");
					div.append(getLabel(field));
					content.append(lightField(field, (v) -> setValue(field, v)));
				}
			}
			default -> {
			}
		}
		div.append(content).append("}");
		return div.toString();
	}

	private <J extends Number> String lightField(BuilderField<T, ?> field, Consumer<ColorLight> valueSetter) {
		var valO = getValue(field, ColorLight.class);
		var value = valO.orElse(new ColorLight((byte) 0, (byte) 15, (byte) 15, (byte) 15));
		int r8 = (value.red << 4) | value.red;
		int g8 = (value.green << 4) | value.green;
		int b8 = (value.blue << 4) | value.blue;
		int rgb = (r8 << 16) | (g8 << 8) | b8;
		var id = binder.bindEvent((val, builder) -> {
			val = val.substring(1);
			int r = Integer.parseInt(val.substring(0, 2), 16);
			int g = Integer.parseInt(val.substring(2, 4), 16);
			int b = Integer.parseInt(val.substring(4, 6), 16);
			byte r4 = (byte) (r >> 4);
			byte g4 = (byte) (g >> 4);
			byte b4 = (byte) (b >> 4);
			valueSetter.accept(new ColorLight((byte) 0, r4, g4, b4));
		});
		binder.setStyle(id, "DefaultColorPickerStyle");
		return "ColorPicker #" + id + " {Anchor: (Width: 120, Height: 120); Value: #" + String.format("%06X", rgb) + "; Format:RgbShort;}";
	}

	private <J extends Number> String numberFieldsHandler(BuilderField<T, ?> field, Class<J> numberClass, Consumer<Double> valueSetter) {

		var valO = getValue(field, numberClass);
		if(valO.isPresent()) {
			var value = valO.get();
			var format = new NumberFieldFormat();
			for(Validator<?> v : field.getValidators()) {
				if(v instanceof GlyphSettingsValidators.StepRangeValidator<?> val) {
					format.withMaxDecimalPlaces(1).withStep(val.step.floatValue()).withMaxValue(val.max.floatValue()).withMinValue(val.min.floatValue());
					break;
				} else if(v instanceof GlyphSettingsValidators.SliderRangeValidator<?> val) {
					return generateSlider(val.min.doubleValue(), val.max.doubleValue(), value.doubleValue(), val.step.doubleValue(), valueSetter);
				}
			}
			return generateNumberField(format, value.doubleValue(), valueSetter);
		}
		return "";
	}

	private String generateNumberField(NumberFieldFormat format, double initialValue, Consumer<Double> onChange) {
		AtomicReference<Double> value = new AtomicReference<>(initialValue);
		var div1 = new StringBuilder("Group{ LayoutMode:Left; ");
		var idIn = binder.bindEventDouble((v, builder) -> {
			value.set(v);
			onChange.accept(value.get());
		});
		var idL = binder.bindButton((b) -> {
			value.set(format.parse(value.get() - 1));
			onChange.accept(value.get());
			b.set("#" + idIn + ".Value", value.get());
		});
		var idM = binder.bindButton((b) -> {

			value.set(format.parse(value.get() + 1));
			onChange.accept(value.get());
			b.set("#" + idIn + ".Value", value.get());
		});

		//binder.setStyle(idL, "SliderStyle", "DefaultSliderStyle");
		binder.setStyle(idIn, "DefaultInputFieldStyle");
		binder.setStyle(idIn, "PlaceholderStyle", "DefaultInputFieldPlaceholderStyle");
		binder.setStyle(idIn, "Background", "InputBoxBackground");
		binder.setStyle(idL, "SmallSecondaryTextButtonStyle");
		binder.setStyle(idM, "SmallSecondaryTextButtonStyle");
		div1.append("TextButton #").append(idL).append(" {Text:\"<\"; Padding: (Horizontal: 16); Anchor:(Height:32);}");
		div1.append("NumberField #").append(idIn).append(" {Anchor:(Width:80,Height:34);Padding: (Horizontal: 10); Value:").append(value.get()).append("; Format:").append(format).append(";}");
		div1.append("TextButton #").append(idM).append(" {Text:\">\"; Padding: (Horizontal: 16); Anchor:(Height:32);}");
		return div1.append("}").toString();
	}

	private String generateSlider(double min, double max, double value, double step, Consumer<Double> onChange) {
		var factor = 1 / step;
		var div1 = new StringBuilder("Group{ LayoutMode:Left; ");
		var id = binder.bindEventDouble((v, builder) -> {
			double val = v / factor;
			onChange.accept(val);
		});
		binder.setStyle(id, "SliderStyle", "DefaultSliderStyle");
		binder.setStyle(id, "NumberFieldStyle", "DefaultInputFieldStyle");
		div1.append("SliderNumberField #").append(id).append(" { Anchor:(Left:10,Width:150,Height:10); Value:").append((int) (value * factor)).append("; Min:").append((int) (min * factor)).append("; Max:").append((int) (max * factor)).append("; Step:").append((int) (step * factor)).append(";}");

		return div1.append("}").toString();
	}

	private <J> void setValue(BuilderField<T, ?> field, J value) {
		var inst = getter.get();
		((BuilderField<T, J>) field).setValue(inst, value, new ExtraInfo());
		setter.accept(inst);
	}

	private void setValueE(BuilderField<T, Enum<?>> field, Enum<?> value) {
		var inst = getter.get();
		field.setValue(inst, value, new ExtraInfo());
		setter.accept(inst);
	}

	public BsonDocument getBaseState() {
		return codec.encode(getter.get(), new ExtraInfo());
	}

	private <J> Optional<J> getValue(BuilderField<T, ?> field, Class<J> type) {
		return ((KeyedCodec<J>) field.getCodec()).get(getBaseState(), new ExtraInfo());
	}
}