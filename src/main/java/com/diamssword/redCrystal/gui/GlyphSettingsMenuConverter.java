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
import com.hypixel.hytale.codec.codecs.simple.FloatCodec;
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
			case FloatCodec _ -> {
				var div1 = GroupBuilder.group().withLayoutMode(LayoutMode.Left);
				//div.addChild(div1);
				AtomicReference<Float> value = new AtomicReference<>(getValue(field, Float.class).orElse(0f));

				var format = new NumberFieldFormat();
				field.getValidators().forEach(v -> {
					if(v instanceof GlyphSettingsValidators.FloatRangeValidator val) {
						format.withMaxDecimalPlaces(1).withStep(val.step).withMaxValue(val.max).withMinValue(val.min);
					}
				});
				div1.addChild(ButtonBuilder.smallSecondaryTextButton().withText("<").onClick(() -> {

					value.set(value.get() - 1);

					setValue(field, value.get());
				}));
				NumberFieldBuilder input = NumberFieldBuilder.numberInput().withAnchor(new HyUIAnchor().setWidth(100)).withFormat(format)
						.withValue(value.get());
				input.addEventListener(CustomUIEventBindingType.ValueChanged, (v) -> {
					input.withValue(v);
					setValue(field, v.floatValue());
					value.set(v.floatValue());

				});
				div.addChild(input);
				div1.addChild(ButtonBuilder.smallSecondaryTextButton().withText(">"));
			}
			default -> {
			}
		}
		container.addChild(div);
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