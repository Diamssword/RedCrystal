package com.diamssword.redCrystal.gui;

import au.ellie.hyui.builders.*;
import au.ellie.hyui.types.LayoutMode;
import au.ellie.hyui.types.NumberFieldFormat;
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
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
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

	public GlyphSettingsMenuConverter(String glyphId, PlayerRef playerRef, Supplier<T> settingsProvider, Consumer<T> settingsSetter, BuilderCodec<T> codec) {
		this.setter = settingsSetter;
		this.getter = settingsProvider;
		this.playerRef = playerRef;
		this.codec = codec;
		this.glyphId = glyphId;
	}

	private final PlayerRef playerRef;

	public void appendSettings(UIElementBuilder<?> container) {
		codec.getEntries().forEach((k, v) -> {
			for(BuilderField<T, ?> f : v) {
				codecFieldConverter(f, container);
			}
		});

	}

	private String translate(String translateString) {
		var tr = I18nModule.get().getMessage(playerRef.getLanguage(), "server.RedCrystal.glyphSetting." + glyphId + "." + translateString);
		return tr != null ? tr : translateString;
	}

	private void codecFieldConverter(BuilderField<T, ?> field, UIElementBuilder<?> container) {
		var div = GroupBuilder.group().withLayoutMode(LayoutMode.Left).withPadding(HyUIPadding.symmetric(10, 5)).withAnchor(new HyUIAnchor().setHeight(40)).withContentHeight(30);
		var doc = translate(field.getCodec().getKey() + ".desc");
		div.addChild(LabelBuilder.label().withText(translate(field.getCodec().getKey() + ".name")).withPadding(new HyUIPadding().setRight(5)).withTooltipText(doc).withStyle(new HyUIStyle().setAlignment(Alignment.Center)));
		switch(field.getCodec().getChildCodec()) {
			case BooleanCodec _ -> div.addChild(new CheckBoxBuilder().withValue(getValue(field, Boolean.class).orElse(false)).withTooltipText(doc)
					.addEventListener(CustomUIEventBindingType.ValueChanged, (v) -> {
						setValue(field, v);
					}));
			case GlobalGlyphSettings.TypedEnumCodec<?> ec -> {
				var values = ec.clazz.getEnumConstants();

				var drop = DropdownBoxBuilder.dropdownBox().withAnchor(new HyUIAnchor().setWidth(200)).withMaxSelection(1).withTooltipText(doc);
				getValue(field, ec.clazz).ifPresent((v) -> drop.withValue(v.toString()));

				for(Enum<? extends Enum<?>> value : values) {
					drop.addEntry(new DropdownEntryInfo(LocalizableString.fromString(value.toString()), value.toString()));
				}
				drop.addEventListener(CustomUIEventBindingType.ValueChanged, (v) -> {
					setValueE((BuilderField<T, Enum<?>>) field, ec.decode(new BsonString(v), new ExtraInfo()));
					drop.withValue(v);
				});
				div.addChild(drop);
			}
			case IntegerCodec _ -> {
				numberFieldsHandler(div, field, Integer.class, (d) -> setValue(field, d.intValue()));

			}
			case FloatCodec _ -> {
				numberFieldsHandler(div, field, Float.class, (d) -> setValue(field, d.floatValue()));

			}
			case DoubleCodec _ -> {
				numberFieldsHandler(div, field, Double.class, (d) -> setValue(field, d));

			}
			default -> {
			}
		}
		container.addChild(div);
	}

	private <J extends Number> void numberFieldsHandler(UIElementBuilder<?> container, BuilderField<T, ?> field, Class<J> numberClass, Consumer<Double> valueSetter) {

		var valO = getValue(field, numberClass);
		if(valO.isPresent()) {
			var value = valO.get();
			var format = new NumberFieldFormat();
			for(Validator<?> v : field.getValidators()) {
				if(v instanceof GlyphSettingsValidators.StepRangeValidator<?> val) {
					format.withMaxDecimalPlaces(1).withStep(val.step.floatValue()).withMaxValue(val.max.floatValue()).withMinValue(val.min.floatValue());
					break;
				} else if(v instanceof GlyphSettingsValidators.SliderRangeValidator<?> val) {
					container.addChild(generateSlider(val.min.doubleValue(), val.max.doubleValue(), value.doubleValue(), val.step.doubleValue(), valueSetter));
					return;
				}
			}
			container.addChild(generateNumberField(format, value.doubleValue(), valueSetter));
		}
	}

	private GroupBuilder generateNumberField(NumberFieldFormat format, double initialValue, Consumer<Double> onChange) {
		AtomicReference<Double> value = new AtomicReference<>(initialValue);
		NumberFieldBuilder input = NumberFieldBuilder.numberInput().withAnchor(new HyUIAnchor().setWidth(80)).withFormat(format)
				.withValue(value.get());
		var div1 = GroupBuilder.group().withLayoutMode(LayoutMode.Left);
		div1.addChild(ButtonBuilder.smallSecondaryTextButton().withText("<").addEventListenerWithContext(CustomUIEventBindingType.Activating, ButtonBuilder.class, (_, c) -> {
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
		}));
		return div1;
	}

	private GroupBuilder generateSlider(double min, double max, double value, double step, Consumer<Double> onChange) {
		var factor = 1 / step;
		var div1 = GroupBuilder.group().withLayoutMode(LayoutMode.Left);
		var slider = SliderBuilder.gameSlider().withAnchor(new HyUIAnchor().setLeft(10).setWidth(150).setHeight(10)).withValue((int) (value * factor)).withMax((int) (max * factor)).withMin((int) (min * factor)).withStep((int) (step * factor));
		var text = LabelBuilder.label().withText(value + "").withAnchor(new HyUIAnchor().setLeft(5)).withStyle(new HyUIStyle().setAlignment(Alignment.Center));
		slider.addEventListenerWithContext(CustomUIEventBindingType.ValueChanged, Integer.class, (b, ctx) -> {
			double val = b / factor;
			text.withText(val + "");
			slider.withValue(b);
			onChange.accept(val);
			ctx.updatePage(false);
		});
		div1.addChild(slider);
		div1.addChild(text);
		return div1;
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