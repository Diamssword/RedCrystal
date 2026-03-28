package com.diamssword.redCrystal.gui;

import au.ellie.hyui.builders.*;
import au.ellie.hyui.html.TemplateProcessor;
import au.ellie.hyui.types.LayoutMode;
import com.diamssword.redCrystal.storage.GlobalGlyphSettings;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderField;
import com.hypixel.hytale.codec.codecs.simple.BooleanCodec;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.bson.BsonDocument;
import org.bson.BsonString;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class GlyphSettingsMenu {

	private final PlayerRef ref;
	private final Consumer<GlobalGlyphSettings> setter;
	private Supplier<GlobalGlyphSettings> getter;

	public GlyphSettingsMenu(PlayerRef ref, Supplier<GlobalGlyphSettings> settingsProvider, Consumer<GlobalGlyphSettings> settingsSetter) {
		this.ref = ref;
		this.setter = settingsSetter;
		this.getter = settingsProvider;
	}

	public HyUIPage openMenu() {
		TemplateProcessor template = new TemplateProcessor();

		var prototype = PageBuilder.pageForPlayer(ref).loadHtml("Pages/RedCrystal/GlyphSettings.html", template)
				.withLifetime(CustomPageLifetime.CanDismiss);
		prototype.getById("main", GroupBuilder.class).ifPresent(this::appendSettings);
		return prototype.open(ref.getReference().getStore());
	}

	public void appendSettings(UIElementBuilder<?> container) {
		GlobalGlyphSettings.CODEC.getEntries().forEach((k, v) -> {
			for(BuilderField<GlobalGlyphSettings, ?> f : v) {
				codecFieldConverter(f, container);
			}
		});
	}

	private void codecFieldConverter(BuilderField<GlobalGlyphSettings, ?> field, UIElementBuilder<?> container) {
		var div = GroupBuilder.group().withLayoutMode(LayoutMode.Left);
		div.addChild(LabelBuilder.label().withText(field.getCodec().getKey()).withPadding(new HyUIPadding().setRight(5)).withTooltipText(field.getDocumentation()));
		if(field.getCodec().getChildCodec() instanceof BooleanCodec) {
			div.addChild(new CheckBoxBuilder().withValue(getValue(field, Boolean.class).orElse(false)).withTooltipText(field.getDocumentation())
					.addEventListener(CustomUIEventBindingType.ValueChanged, (v) -> {
						setValue(field, v);
					}));

		} else if(field.getCodec().getChildCodec() instanceof GlobalGlyphSettings.TypedEnumCodec<?> ec) {
			var values = ec.clazz.getEnumConstants();

			var drop = DropdownBoxBuilder.dropdownBox().withAnchor(new HyUIAnchor().setWidth(200)).withMaxSelection(1).withTooltipText(field.getDocumentation());
			getValue(field, ec.clazz).ifPresent((v) -> drop.withValue(v.toString()));

			for(Enum<? extends Enum<?>> value : values) {
				drop.addEntry(new DropdownEntryInfo(LocalizableString.fromString(value.toString()), value.toString()));
			}
			drop.addEventListener(CustomUIEventBindingType.ValueChanged, (v) -> {
				setValueE((BuilderField<GlobalGlyphSettings, Enum<?>>) field, ec.decode(new BsonString(v), new ExtraInfo()));
				drop.withValue(v);
			});
			div.addChild(drop);
		}
		container.addChild(div);
	}

	private <T> void setValue(BuilderField<GlobalGlyphSettings, ?> field, T value) {
		var inst = getter.get();
		((BuilderField<GlobalGlyphSettings, T>) field).setValue(inst, value, new ExtraInfo());
		setter.accept(inst);
		//baseState = GlobalGlyphSettings.CODEC.encode(inst, new ExtraInfo());
	}

	private void setValueE(BuilderField<GlobalGlyphSettings, Enum<?>> field, Enum<?> value) {
		var inst = getter.get();
		field.setValue(inst, value, new ExtraInfo());
		setter.accept(inst);
		//baseState = GlobalGlyphSettings.CODEC.encode(inst, new ExtraInfo());
	}

	public BsonDocument getBaseState() {
		return GlobalGlyphSettings.CODEC.encode(getter.get(), new ExtraInfo());
	}

	private <T> Optional<T> getValue(BuilderField<GlobalGlyphSettings, ?> field, Class<T> type) {
		return ((KeyedCodec<T>) field.getCodec()).get(getBaseState(), new ExtraInfo());
	}
}