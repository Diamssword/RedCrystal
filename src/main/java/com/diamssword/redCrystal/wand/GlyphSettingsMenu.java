package com.diamssword.redCrystal.wand;

import au.ellie.hyui.builders.*;
import au.ellie.hyui.html.TemplateProcessor;
import au.ellie.hyui.types.LayoutMode;
import com.diamssword.redCrystal.storage.GlobalGlyphSettings;
import com.diamssword.redCrystal.storage.RedElement;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderField;
import com.hypixel.hytale.codec.codecs.simple.BooleanCodec;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.bson.BsonDocument;
import org.bson.BsonString;

import java.util.Optional;


public class GlyphSettingsMenu {

	private final RedElement element;
	private final PlayerRef ref;
	private final Player player;
	private BsonDocument baseState;

	public GlyphSettingsMenu(PlayerRef ref, RedElement element) {
		this.element = element;
		this.ref = ref;
		this.player = ref.getReference().getStore().getComponent(ref.getReference(), Player.getComponentType());
		this.baseState = GlobalGlyphSettings.CODEC.encode(element.getSettings(), new ExtraInfo());
	}

	public HyUIPage openMenu() {
		TemplateProcessor template = new TemplateProcessor();

		var prototype = PageBuilder.pageForPlayer(ref).loadHtml("Pages/RedCrystal/GlyphSettings.html", template)
				.withLifetime(CustomPageLifetime.CanDismiss);
		prototype.getById("main", GroupBuilder.class).ifPresent((div) -> {
			GlobalGlyphSettings.CODEC.getEntries().forEach((k, v) -> {
				for(BuilderField<GlobalGlyphSettings, ?> f : v) {
					codecFieldConverter(f, div);
				}
			});
		});
		return prototype.open(ref.getReference().getStore());
	}

	private void codecFieldConverter(BuilderField<GlobalGlyphSettings, ?> field, GroupBuilder container) {
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
			});
			div.addChild(drop);
		}
		container.addChild(div);
	}

	private <T> void setValue(BuilderField<GlobalGlyphSettings, ?> field, T value) {
		var inst = GlobalGlyphSettings.CODEC.decode(baseState, new ExtraInfo());
		((BuilderField<GlobalGlyphSettings, T>) field).setValue(inst, value, new ExtraInfo());
		element.updateSettings(inst);
		baseState = GlobalGlyphSettings.CODEC.encode(inst, new ExtraInfo());
	}

	private void setValueE(BuilderField<GlobalGlyphSettings, Enum<?>> field, Enum<?> value) {
		var inst = GlobalGlyphSettings.CODEC.decode(baseState, new ExtraInfo());
		field.setValue(inst, value, new ExtraInfo());
		element.updateSettings(inst);
		baseState = GlobalGlyphSettings.CODEC.encode(inst, new ExtraInfo());
	}


	private <T> Optional<T> getValue(BuilderField<GlobalGlyphSettings, ?> field, Class<T> type) {
		return ((KeyedCodec<T>) field.getCodec()).get(baseState, new ExtraInfo());
	}
}