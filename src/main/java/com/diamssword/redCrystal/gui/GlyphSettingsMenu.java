package com.diamssword.redCrystal.gui;

import com.diamssword.redCrystal.storage.GlobalGlyphSettings;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.bson.BsonDocument;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class GlyphSettingsMenu extends InteractiveCustomUIPage<UniversalDataBinding> {

	private final Consumer<GlobalGlyphSettings> setter;
	private final Supplier<GlobalGlyphSettings> getter;
	private GlyphSettingsMenuConverter<?> sepcific;
	private UniversalEventBinder binder = new UniversalEventBinder();
	private String ui = "Pages/RedCrystal/GlyphSettings.ui";
	private Runnable onClose;

	public GlyphSettingsMenu(@Nonnull PlayerRef ref, Supplier<GlobalGlyphSettings> settingsProvider, Consumer<GlobalGlyphSettings> settingsSetter) {
		super(ref, CustomPageLifetime.CanDismiss, UniversalDataBinding.CODEC);
		this.setter = settingsSetter;
		this.getter = settingsProvider;

	}

	@Override
	public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
		super.onDismiss(ref, store);
		if(onClose != null)
			onClose.run();
	}

	public GlyphSettingsMenu(PlayerRef ref) {
		super(ref, CustomPageLifetime.CanDismiss, UniversalDataBinding.CODEC);
		this.setter = null;
		this.getter = null;

	}

	@Override
	public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {

		commandBuilder.append(ui);
		appendSettings("Main", commandBuilder, eventBuilder);
	}

	public <T> GlyphSettingsMenu withSpecific(String id, Supplier<BsonDocument> getter, Consumer<BsonDocument> setter, BuilderCodec<T> codec) {
		this.sepcific = new GlyphSettingsMenuConverter<T>(binder, id, playerRef, () -> codec.decode(getter.get(), new ExtraInfo()), (res) -> setter.accept(codec.encode(res, new ExtraInfo())), codec);
		return this;
	}

	public void openAsSubMenu(Runnable onClose) {
		ui = "Pages/RedCrystal/GlyphSettingsCentered.ui";
		Player playerComponent = (Player) playerRef.getReference().getStore().getComponent(playerRef.getReference(), Player.getComponentType());
		if(playerComponent != null)
			playerComponent.getPageManager().openCustomPage(playerRef.getReference(), playerRef.getReference().getStore(), this);
		this.onClose = onClose;
		/*prototype.onDismiss((page, bool) -> {
			onClose.run();
		});*/
	}

	public void appendSettings(String key, UICommandBuilder builder, UIEventBuilder eventBuilder) {
		if(setter != null) {
			var global = new GlyphSettingsMenuConverter<>(binder, "global", playerRef, getter, setter, GlobalGlyphSettings.CODEC);
			global.appendSettings(key, builder);
		}
		if(sepcific != null) {
			sepcific.appendSettings(key, builder);
		}
		binder.setEventBuilder(eventBuilder, builder);

	}


	@Override
	public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull UniversalDataBinding data) {
		UICommandBuilder commandBuilder = new UICommandBuilder();
		if(data.elementId != null) {
			binder.onReceived(commandBuilder, data);
		}
		sendUpdate(commandBuilder, false);
	}
}