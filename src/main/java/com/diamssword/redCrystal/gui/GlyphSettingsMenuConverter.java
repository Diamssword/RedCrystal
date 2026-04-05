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

	private String codecFieldConverter(BuilderField<T, ?> field) {
		var div = new StringBuilder("Group{LayoutMode:Left; Padding:(Top:10,Bottom:10,Left:5,Right:5);Anchor: (Height:40); ");

		var doc = translate(field.getCodec().getKey() + ".desc");
		div.append("Label{Text:\"").append(translate(field.getCodec().getKey() + ".name")).append("\"; Padding:(Right:5);TooltipText:\"").append(doc).append("\"; Style: (HorizontalAlignment:Center);}");
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
			default -> {
			}
		}
		div.append(content).append("}");
		return div.toString();
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
		/*div1.addChild(ButtonBuilder.smallSecondaryTextButton().withText("<").addEventListenerWithContext(CustomUIEventBindingType.Activating, ButtonBuilder.class, (_, c) -> {
			value.set(value.get() - 1);
			input.withValue(value.get());
			onChange.accept(value.get());
			c.updatePage(false);
		}));


		input.addEventListener(CustomUIEventBindingType.ValueChanged, (v) -> {
			input.withValue(v);
			value.set(v);
			onChange.accept(value.get());

		});
		div1.addChild(input);
		div1.addChild(ButtonBuilder.smallSecondaryTextButton().withText(">").addEventListenerWithContext(CustomUIEventBindingType.Activating, ButtonBuilder.class, (_, c) -> {
			value.set(value.get() + 1);
			input.withValue(value.get());
			onChange.accept(value.get());
			c.updatePage(false);
		})); */
		return div1.append("}").toString();
	}

	private String generateSlider(double min, double max, double value, double step, Consumer<Double> onChange) {
		var factor = 1 / step;
		var div1 = new StringBuilder("Group{ LayoutMode:Left; ");
		var id = binder.bindEventDouble((v, builder) -> {
			double val = v / factor;
			//slider.withValue(v);
			onChange.accept(val);
			//ctx.updatePage(false);
		});
		binder.setStyle(id, "SliderStyle", "DefaultSliderStyle");
		binder.setStyle(id, "NumberFieldStyle", "DefaultInputFieldStyle");
		div1.append("SliderNumberField #").append(id).append(" { Anchor:(Left:10,Width:150,Height:10); Value:").append((int) (value * factor)).append("; Min:").append((int) (min * factor)).append("; Max:").append((int) (max * factor)).append("; Step:").append((int) (step * factor)).append(";}");
		//var text = LabelBuilder.label().withText(value + "").withAnchor(new HyUIAnchor().setLeft(5)).withStyle(new HyUIStyle().setAlignment(Alignment.Center));
		/*slider.addEventListenerWithContext(CustomUIEventBindingType.ValueChanged, Integer.class, (b, ctx) -> {
			double val = b / factor;
			text.withText(val + "");
			slider.withValue(b);
			onChange.accept(val);
			ctx.updatePage(false);
		});
		div1.addChild(slider);
		div1.addChild(text);

		 */
		return div1.append("}").toString();
	}

	private <J> void setValue(BuilderField<T, ?> field, J value) {
		var inst = getter.get();
		((BuilderField<T, J>) field).setValue(inst, value, new ExtraInfo());
		setter.accept(inst);
		//baseState = GlobalGlyphSettings.CODEC.encode(inst, new ExtraInfo());
	}

	private void setValueE(BuilderField<T, Enum<?>> field, Enum<?> value) {
		var inst = getter.get();
		field.setValue(inst, value, new ExtraInfo());
		setter.accept(inst);
		//baseState = GlobalGlyphSettings.CODEC.encode(inst, new ExtraInfo());
	}

	public BsonDocument getBaseState() {
		return codec.encode(getter.get(), new ExtraInfo());
	}

	private <J> Optional<J> getValue(BuilderField<T, ?> field, Class<J> type) {
		return ((KeyedCodec<J>) field.getCodec()).get(getBaseState(), new ExtraInfo());
	}
}