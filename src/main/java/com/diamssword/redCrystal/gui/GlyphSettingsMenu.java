package com.diamssword.redCrystal.gui;

import au.ellie.hyui.builders.*;
import au.ellie.hyui.html.TemplateProcessor;
import au.ellie.hyui.types.LayoutMode;
import com.diamssword.redCrystal.storage.GlobalGlyphSettings;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
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
	private final Supplier<GlobalGlyphSettings> getter;
	private GlyphSettingsMenuConverter<?> sepcific;

	public GlyphSettingsMenu(PlayerRef ref, Supplier<GlobalGlyphSettings> settingsProvider, Consumer<GlobalGlyphSettings> settingsSetter) {
		this.ref = ref;
		this.setter = settingsSetter;
		this.getter = settingsProvider;

	}

	public GlyphSettingsMenu(PlayerRef ref) {
		this.ref = ref;
		this.setter = null;
		this.getter = null;

	}

	public <T> GlyphSettingsMenu withSpecific(String id, Supplier<BsonDocument> getter, Consumer<BsonDocument> setter, BuilderCodec<T> codec) {
		this.sepcific = new GlyphSettingsMenuConverter<T>(id, ref, () -> codec.decode(getter.get(), new ExtraInfo()), (res) -> setter.accept(codec.encode(res, new ExtraInfo())), codec);
		return this;
	}

	public HyUIPage openAsSubMenu(Runnable onClose) {
		TemplateProcessor template = new TemplateProcessor();

		var prototype = PageBuilder.pageForPlayer(ref).loadHtml("Pages/RedCrystal/GlyphSettingsCentered.html", template)
				.withLifetime(CustomPageLifetime.CanDismiss);
		prototype.getById("main", GroupBuilder.class).ifPresent(this::appendSettings);
		prototype.onDismiss((page, bool) -> {
			onClose.run();
		});
		return prototype.open(ref.getReference().getStore());
	}

	public HyUIPage openMenu() {
		TemplateProcessor template = new TemplateProcessor();

		var prototype = PageBuilder.pageForPlayer(ref).loadHtml("Pages/RedCrystal/GlyphSettings.html", template)
				.withLifetime(CustomPageLifetime.CanDismiss);
		prototype.getById("main", GroupBuilder.class).ifPresent(this::appendSettings);
		return prototype.open(ref.getReference().getStore());
	}

	public void appendSettings(UIElementBuilder<?> container) {
		if(setter != null) {
			var global = new GlyphSettingsMenuConverter<>("global", ref, getter, setter, GlobalGlyphSettings.CODEC);
			global.appendSettings(container);
		}
		if(sepcific != null) {
			sepcific.appendSettings(container);
		}

	}

}